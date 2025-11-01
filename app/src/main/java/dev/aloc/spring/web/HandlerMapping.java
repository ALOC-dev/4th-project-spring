package dev.aloc.spring.web;

import dev.aloc.spring.BeanFactory;
import dev.aloc.spring.annotation.Controller;
import dev.aloc.spring.annotation.GetMapping;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL 요청과 그 요청을 처리할 Controller 메서드를 매핑해 관리하는 컴포넌트
 */
public class HandlerMapping {

  // URL -> HandlerMethod(컨트롤러 인스턴스 + 메서드) 매핑 테이블
  private final Map<String, HandlerMethod> handlerMethods = new ConcurrentHashMap<>();

  /**
   * BeanFactory를 스캔해 @Controller와 @GetMapping을 분석하고
   * handlerMethods 맵을 채우는 초기화 메서드
   *
   * @param bf BeanFactory
   * @param componentClasses @Component가 붙은 모든 클래스 목록
   */
  public void initialize(BeanFactory bf, Set<Class<?>> componentClasses) {
    System.out.println("HandlerMapping 초기화 시작!");

    // 6. @Controller 클래스의 @GetMapping 스캔 -> 매핑 테이블 구성
    for (Class<?> clazz : componentClasses) {
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
        if (handlerMethods.containsKey(url)) {
          System.out.println("[DispatcherServlet] WARNING: duplicate @GetMapping URL '" + url +
              "' → " + handlerMethods.get(url) + " (keeping first, skipping " + m + ")");
          continue;
        }

        m.setAccessible(true);
        HandlerMethod handlerMethod = new HandlerMethod(controller, m);
        handlerMethods.put(url, handlerMethod);

        System.out.println("[DispatcherServlet] GET handler mapped: " + url +
            " -> " + clazz.getSimpleName() + "." + m.getName());
      }
    }
  }

  /**
   * URL에 매핑된 HandlerMethod를 반환
   */
  public HandlerMethod getHandler(String url) {
    return handlerMethods.get(url);
  }

  /**
   * health 체크용: 매핑된 핸들러 개수 반환
   */
  public int getHandlerCount() {
    return handlerMethods.size();
  }
}