package dev.aloc.spring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class AspectProxyHandler implements InvocationHandler {
    
    private final Object target;
    private final List<MethodInterceptor> interceptors;
    
    public AspectProxyHandler(Object target, List<MethodInterceptor> interceptors) {
        this.target = target;
        this.interceptors = interceptors; // 해당 메소드를 실행할 때 같이 실행할 모든 공통기능(인터셉터)들
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        // invocation 객체 안에 타겟 클래스, 메소드, 매개변수, 공통기능 목록을 넣어주기
        MethodInvocation invocation =
            new ReflectiveMethodInvocation(target, method, args, interceptors);
        
        return invocation.proceed(); // 체인으로 실행
    }
}
