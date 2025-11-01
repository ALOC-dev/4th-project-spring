package dev.aloc.spring.web;

import static java.lang.System.out;

import dev.aloc.spring.BeanFactory;
import dev.aloc.spring.ClasspathComponentScanner;
import dev.aloc.spring.ComponentScanner;
import dev.aloc.spring.SimpleBeanFactory;
import dev.aloc.spring.testcomponents.MyRepository;
import dev.aloc.spring.testcomponents.MyService;

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

    private HandlerMapping handlerMapping;
    private HandlerAdapter handlerAdapter;

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
            this.beanFactory = bf;

            // 5. 현재 서블릿 필드에 캐시
            this.handlerMapping = new HandlerMapping();
            this.handlerMapping.initialize(bf, components);

            this.handlerAdapter = new HandlerAdapter();

            out.println("[DispatcherServlet] 초기화 성공!");
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

        String path = getRequestPath(req);

        // health 체크
        if ("/health".equals(path)) {
            writeHealth(resp, bf);
            return;
        }

        // 1. 핸들러 '찾기' (HandlerMapping에게 위임)
        HandlerMethod handler = handlerMapping.getHandler(path);

        if ("GET".equalsIgnoreCase(req.getMethod()) && handler != null) {
            try {
                // 2. 핸들러 '실행' (HandlerAdapter에게 위임)
                handlerAdapter.handle(req, resp, handler);
                return; // 응답 완료

            } catch (Exception ex) {
                resp.reset();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "핸들러 실행 중 오류: " + ex.getCause());
                return;
            }
        }

        // 매핑된 핸들러가 없는 경우 (404 Not Found)
        handleNotFound(resp, path);
    }

    private String getRequestPath(HttpServletRequest req) {
        // 요청 URL 수집
        String rawUri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = (ctx != null && !ctx.isEmpty())
            ? rawUri.substring(ctx.length())
            : rawUri;
        return path.isEmpty() ? "/" : path;
    }

    private void handleNotFound(HttpServletResponse resp, String path) throws IOException {
        // 매핑이 없으면, 기존 기본 JSON 응답
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        try (PrintWriter writer = resp.getWriter()) {
            out.print("{\"error\":\"Not Found\", \"message\":\"No handler found for GET " + path + "\", \"status\": 404}");
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
                """.formatted(handlerMapping.getHandlerCount(), hasSvc, hasRepo));
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