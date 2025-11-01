package dev.aloc.spring.web;

import dev.aloc.spring.BeanFactory;
import dev.aloc.spring.ClasspathComponentScanner;
import dev.aloc.spring.ComponentScanner;
import dev.aloc.spring.SimpleBeanFactory;
import dev.aloc.spring.testcomponents.MyRepository;
import dev.aloc.spring.testcomponents.MyService;
import dev.aloc.spring.annotation.Controller;
import dev.aloc.spring.annotation.GetMapping;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.WebInitParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.lang.reflect.Method; // 메서드 리플렉션
import java.util.Map; // 매핑 테이블
import java.util.concurrent.ConcurrentHashMap; // 동시성 안전 맵

// 이후 들어오는 모든 URL을 수집
@WebServlet(
        name = "dispatcherServlet",

        // 모든 요청을 이 서블릿으로 라우팅
        urlPatterns = "/*",

        // 서버 기동 시, 즉시 init() 실행
        loadOnStartup = 1,

        // 서블릿 초기 파라미터
        initParams = {
                @WebInitParam(name = DispatcherServlet.INIT_PARAM_BASE_PACKAGE, value = "dev.aloc.spring")
        }
)

public class DispatcherServlet extends HttpServlet {
    // BeanFactory 넣고 꺼낼 때 사용할 키 이름
    public static final String CTX_BEAN_FACTORY = "beanFactory";
    // init param 이름
    public static final String INIT_PARAM_BASE_PACKAGE = "basePackage";
    // 아 서블릿 인스턴스가 캐싱해두는 BeanFactory
    private transient BeanFactory beanFactory;

    // URL → 컨트롤러 인스턴스, Method 매핑 테이블
    private final Map<String, Method> getHandlerMethods = new ConcurrentHashMap<>();
    private final Map<String, Object> getHandlerControllers = new ConcurrentHashMap<>();

    // 초기화
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // 1. 스캔 대상 패키지 결정
        String basePackage = config.getInitParameter(INIT_PARAM_BASE_PACKAGE);
        if (basePackage == null || basePackage.isBlank()) {
            basePackage = "dev.aloc.spring";
        }

        try {
            // 2. @Component 스캔
            ComponentScanner scanner = new ClasspathComponentScanner();
            Set<Class<?>> components = scanner.scan(basePackage);

            // 3. BeanFactory 생성 및 등록
            BeanFactory bf = new SimpleBeanFactory();
            bf.registerBeans(components);

            // 4. ServletContext에 보관 (다른 서블릿/필터에서도 접근 가능)
            ServletContext sc = config.getServletContext();
            sc.setAttribute(CTX_BEAN_FACTORY, bf);

            // 5. 현재 서블릿 필드에 캐시
            this.beanFactory = bf;

            // 6. @Controller 클래스의 @GetMapping 스캔 -> 매핑 테이블 구성
            for (Class<?> clazz : components) {
                if (!clazz.isAnnotationPresent(Controller.class)) continue;

                // BeanFactory에서 컨트롤러 인스턴스 가져옴
                Object controller = null;
                try {
                    controller = bf.getExistingBean(clazz);
                }
                catch (Exception ignored) {
                    /* 스킵 */
                }
                if (controller == null) continue;

                for (Method m : clazz.getDeclaredMethods()) {
                    if (!m.isAnnotationPresent(GetMapping.class)) continue;

                    String url = m.getAnnotation(GetMapping.class).value();
                    if (url == null || url.isBlank()) continue;

                    // URL 정규화
                    if (!url.startsWith("/")) url = "/" + url;

                    // 충돌 방지 (이미 등록된 URL이면 덮어쓰지 않고 경고)
                    if (getHandlerMethods.containsKey(url)) {
                        System.out.println("[DispatcherServlet] WARNING: duplicate @GetMapping URL '" + url +
                                "' → " + getHandlerMethods.get(url) + " (keeping first, skipping " + m + ")");
                        continue;
                    }

                    m.setAccessible(true);
                    getHandlerMethods.put(url, m);
                    getHandlerControllers.put(url, controller);

                    System.out.println("[DispatcherServlet] GET handler mapped: " + url +
                            " -> " + clazz.getSimpleName() + "." + m.getName());
                }
            }
        }
        catch (Exception e) {
            // 초기화 실패 시, 서블릿 구동 중단
            throw new ServletException("DispatcherServlet init 실패: " + e.getMessage(), e);
        }
    }

    // 요청 처리
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // BeanFactory 확보
        BeanFactory bf = getBeanFactoryFromContext();
        // 없으면 500
        if (bf == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "BeanFactory 를 초기화하지 못했습니다.");
            return;
        }

        // 요청 URL 수집
        String rawUri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = (ctx != null && !ctx.isEmpty())
                ? rawUri.substring(ctx.length())
                : rawUri;
        if (path.isEmpty()) path = "/";

        // health 체크
        if ("/health".equals(path)) {
            writeHealth(resp, bf);
            return;
        }

        // 메서드 매핑에 있으면, 해당 컨트롤러 메서드 호출
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            Method handler = getHandlerMethods.get(path);
            Object controller = getHandlerControllers.get(path);
            if (handler != null && controller != null) {
                try {
                    Object result;

                    // 1. 시그니처가 HttpServletRequest, HttpServletResponse인 경우
                    if (handler.getParameterCount() == 2 &&
                            handler.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class) &&
                            handler.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class)) {

                        result = handler.invoke(controller, req, resp);

                        if (resp.isCommitted()) return;

                        // 2.파라미터 없는 경우
                    } else if (handler.getParameterCount() == 0) {
                        result = handler.invoke(controller);
                    } else {
                        // 규약 외의 시그니처는 400
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 핸들러 시그니처: " + handler);
                        return;
                    }

                    // 반환값 처리
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("application/json");
                    try (PrintWriter out = resp.getWriter()) {
                        // String 이면 문자열로 응답
                        if (result instanceof String s) {
                            out.print(s);
                        }
                        // 그 외에는 toString() 결과로 응답
                        else {
                            out.print(result == null ? "null" : String.valueOf(result));
                        }
                    }
                    return;

                } catch (Exception ex) {
                    resp.reset();
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "핸들러 실행 중 오류: " + ex.getCause());
                    return;
                }
            }
        }

        // 매핑이 없으면, 기존 기본 JSON 응답
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try (PrintWriter out = resp.getWriter()) {
            MyService myService = null;
            MyRepository myRepository = null;
            try { myService = bf.getExistingBean(MyService.class); } catch (Exception ignored) {}
            try { myRepository = bf.getExistingBean(MyRepository.class); } catch (Exception ignored) {}

            out.print("""
                {
                  "message": "DispatcherServlet is running",
                  "requestUri": "%s",
                  "mappedGetHandlers": %d,
                  "beans": {
                    "MyService": %s,
                    "MyRepository": %s
                  }
                }
                """.formatted(path, getHandlerMethods.size(), myService != null, myRepository != null));
        } catch (Exception e) {
            resp.reset();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "요청 처리 중 오류: " + e.getMessage());
        }
    }

    private void writeHealth(HttpServletResponse resp, BeanFactory bf) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            boolean hasSvc = false;
            boolean hasRepo = false;
            try { hasSvc = bf.getExistingBean(MyService.class) != null; } catch (Exception ignored) {}
            try { hasRepo = bf.getExistingBean(MyRepository.class) != null; } catch (Exception ignored) {}
            out.print("""
                {
                  "status": "UP",
                  "beanFactory": "initialized",
                  "mappedGetHandlers": %d,
                  "beans": {
                    "MyService": %s,
                    "MyRepository": %s
                  }
                }
                """.formatted(getHandlerMethods.size(), hasSvc, hasRepo));
        }
    }

    // BeanFactory 조회
    private BeanFactory getBeanFactoryFromContext() {
        // 캐시에 있으면, 그걸 쓰고
        if (this.beanFactory != null) return this.beanFactory;
        // 캐시에 없으면, ServletContext에서 꺼내서 캐싱 및 반환
        ServletContext sc = getServletContext();
        Object bf = (sc != null) ? sc.getAttribute(CTX_BEAN_FACTORY) : null;
        if (bf instanceof BeanFactory) {
            this.beanFactory = (BeanFactory) bf; // 캐시
        }
        return this.beanFactory;
    }
}