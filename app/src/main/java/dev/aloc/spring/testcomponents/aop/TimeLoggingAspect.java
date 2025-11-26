package dev.aloc.spring.testcomponents.aop;

import java.lang.reflect.Method;

// AOP가 실제로 동작하는지 테스트 하기 위한 Aspect
public class TimeLoggingAspect {
    public Object invoke(Method method, Object target, Object[] args) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return method.invoke(target, args);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("[TimeLogging] " + method.getName() +
                    " took " + (end - start) + " ms");
        }
    }
}