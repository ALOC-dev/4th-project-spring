package dev.aloc.spring.web;

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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * HTTP 요청을 받아 애플리케이션 레이어(BeanFactory)로 연결하는 기본 Dispatcher 서블릿.
 * 역할:
 * 1) init(): 서블릿 초기화 시 @Component 스캔 후 BeanFactory에 등록하여 애플리케이션 준비
 * 2) service(): 요청을 처리(데모: 간단한 상태/테스트 응답)하고, 예외 시 500 반환
 * Note:
 * - init-param "basePackage"가 있으면 그 패키지를 스캔하고, 없으면 기본값 "dev.aloc.spring" 사용
 * - 생성된 BeanFactory는 ServletContext에 "beanFactory" 이름으로 보관
 */
public class DispatcherServlet extends HttpServlet {

    public static final String CTX_BEAN_FACTORY = "beanFactory";
    public static final String INIT_PARAM_BASE_PACKAGE = "basePackage";
    private transient BeanFactory beanFactory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // 1) 스캔 대상 패키지 결정 (web.xml 또는 애노테이션 init-param)
        String basePackage = config.getInitParameter(INIT_PARAM_BASE_PACKAGE);
        if (basePackage == null || basePackage.isBlank()) {
            basePackage = "dev.aloc.spring";
        }

        try {
            // 2) @Component 스캔
            ComponentScanner scanner = new ClasspathComponentScanner();
            Set<Class<?>> components = scanner.scan(basePackage);

            // 3) BeanFactory 생성 및 등록
            BeanFactory bf = new SimpleBeanFactory();
            bf.registerBeans(components);

            // 4) ServletContext에 보관 (다른 서블릿/필터에서도 접근 가능)
            ServletContext sc = config.getServletContext();
            sc.setAttribute(CTX_BEAN_FACTORY, bf);

            // 5) 필드에도 보관해 service()에서 바로 사용
            this.beanFactory = bf;
        } catch (Exception e) {
            // 초기화 실패 시 서블릿 구동 중단
            throw new ServletException("DispatcherServlet init 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 모든 HTTP 메서드에 대해 공통 처리.
     * 여기서는 데모로 상태 점검용 JSON을 내려준다.
     * 이후 @Controller/@RequestMapping 같은 라우팅 레이어가 생기면
     * 요청 경로/메서드에 따라 핸들러를 찾아 실행하도록 확장하면 된다.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 혹시 모를 NPE 방지: init()이 실패했거나 컨텍스트에서 못 찾은 경우
        BeanFactory bf = getBeanFactoryFromContext();
        if (bf == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "BeanFactory 를 초기화하지 못했습니다.");
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try (PrintWriter out = resp.getWriter()) {
            // 데모: 등록된 테스트 빈을 실제로 한 번 꺼내보고,
            //       간단한 상태 정보를 JSON으로 응답
            MyService myService = null;
            MyRepository myRepository = null;
            try {
                myService = bf.getExistingBean(MyService.class);
            } catch (Exception ignored) {}
            try {
                myRepository = bf.getExistingBean(MyRepository.class);
            } catch (Exception ignored) {}

            String path = req.getRequestURI();

            // 아주 간단한 라우팅 데모: /health 로 오면 헬스체크
            if ("/health".equals(path)) {
                out.print("""
                    {
                      "status": "UP",
                      "beanFactory": "initialized",
                      "beans": {
                        "MyService": %s,
                        "MyRepository": %s
                      }
                    }
                    """.formatted(myService != null, myRepository != null));
                return;
            }

            // 기본 응답 (확장 전 단계의 심플한 디스패처 동작)
            out.print("""
                {
                  "message": "DispatcherServlet is running",
                  "requestUri": "%s",
                  "beans": {
                    "MyService": %s,
                    "MyRepository": %s
                  }
                }
                """.formatted(path, myService != null, myRepository != null));
        } catch (Exception e) {
            // 예외 발생 시 500
            resp.reset();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "요청 처리 중 오류: " + e.getMessage());
        }
    }

    private BeanFactory getBeanFactoryFromContext() {
        if (this.beanFactory != null) return this.beanFactory;
        ServletContext sc = getServletContext();
        Object bf = (sc != null) ? sc.getAttribute(CTX_BEAN_FACTORY) : null;
        if (bf instanceof BeanFactory) {
            this.beanFactory = (BeanFactory) bf;
        }
        return this.beanFactory;
    }
}
