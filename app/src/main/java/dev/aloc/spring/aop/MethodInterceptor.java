package dev.aloc.spring.aop;

public interface MethodInterceptor {
    
    // 이 인터페이스의 구현체에서 메소드 실행 전후 기능을 추가 (invoke() 메소드 안에 정의)
    Object invoke(MethodInvocation invocation) throws Throwable;
}
