package dev.aloc.spring.aop;

public interface MethodInterceptor {
    
    // 이 인터페이스의 구현체에서 실제 메소드 실행 전후의 기능을 추가함 (invoke() 메소드 안에 정의)
    
    Object invoke(MethodInvocation invocation) throws Throwable;
}
