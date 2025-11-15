package dev.aloc.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

// InvocationHandler 구현체 (공통 로직: Before / After 로그)
class LoggingInvocationHandler implements InvocationHandler {

    private final Object target; // 실제 객체

    public LoggingInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Before 출력
        System.out.println("[Before] 메서드 이름: " + method.getName());

        // 원본 메서드 호출
        Object result = method.invoke(target, args);

        // After 출력
        System.out.println("[After] 메서드 이름: " + method.getName());

        return result;
    }
}
