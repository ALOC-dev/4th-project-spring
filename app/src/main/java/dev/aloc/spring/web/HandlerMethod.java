package dev.aloc.spring.web;

import java.lang.reflect.Method;

/**
 * URL에 매핑된 컨트롤러의 인스턴스와 실행할 메서드를
 * 하나로 묶어 관리하는 객체
 */
public class HandlerMethod {
  private final Object controller; // 메서드를 실행할 컨트롤러 인스턴스
  private final Method method;     // 실행할 메서드

  public HandlerMethod(Object controller, Method method) {
    this.controller = controller;
    this.method = method;
  }

  public Object getController() {
    return controller;
  }

  public Method getMethod() {
    return method;
  }
}