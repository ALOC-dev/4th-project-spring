package dev.aloc.spring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AspectProxyHandler implements InvocationHandler {
    
    private final Object target;
    
    public AspectProxyHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 실행시간 로그 찍기
        long startTime = System.nanoTime();
        Object result = method.invoke(target, args);
        long endTime = System.nanoTime();
        
        System.out.println(
            target.getClass().getName() + "." + method.getName() +
                "실행 시간: " + (endTime - startTime) / 100000 + " ms");
        return result;
    }
}
