package dev.aloc.spring.aop;

import dev.aloc.spring.annotation.TimeLog;

@Aspect
@Around(TimeLog.class) // @TimeLog 어노테이션이 붙은 메소드에만 적용됨
public class TimeLoggingAspect implements MethodInterceptor {
    
    // 일단 인터셉터 하나(=공통기능 하나로 생각하면됨)를 Aspect로 쓸 수 있도록 만들었습니다
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        
        long startTime = System.nanoTime();
        Object result = invocation.proceed();
        long endTime = System.nanoTime();
        
        System.out.println(invocation.getTarget().getClass().getName() + "." +
            invocation.getMethod().getName() + " 실행 시간: " +
            (endTime - startTime) / 1000000 + " ms");
        
        return result;
    }
}
