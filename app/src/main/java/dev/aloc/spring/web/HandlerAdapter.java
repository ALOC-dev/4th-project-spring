package dev.aloc.spring.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * HandlerMapping이 찾아낸 HandlerMethod를
 * 실제로 실행하고 응답을 처리
 */
public class HandlerAdapter {

  /**
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @param handler 매핑된 HandlerMethod (컨트롤러 + 메서드)
   * @throws Exception
   */
  public void handle(HttpServletRequest req, HttpServletResponse resp, HandlerMethod handler) throws Exception {

    Object controller = handler.getController();
    Method method = handler.getMethod();

    // 해당 컨트롤러 메서드 호출
    try {
      Object result;

      // 1. 시그니처가 HttpServletRequest, HttpServletResponse인 경우
      if (method.getParameterCount() == 2 &&
          method.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class) &&
          method.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class)) {

        result = method.invoke(controller, req, resp);
        if (resp.isCommitted()) return;

      // 2.파라미터 없는 경우
      } else if (method.getParameterCount() == 0) {
        result = method.invoke(controller);
      } else {
        // 규약 외의 시그니처는 400
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 핸들러 시그니처: " + method);
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
    } catch (Exception ex) {
      // 핸들러 실행 중 발생한 예외를 다시 던져서 Servlet이 처리하도록 함
      throw new Exception("핸들러 실행 중 오류: " + ex.getCause(), ex);
    }
  }
}