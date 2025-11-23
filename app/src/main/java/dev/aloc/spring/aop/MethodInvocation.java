package dev.aloc.spring.aop;

import java.lang.reflect.Method;

public interface MethodInvocation {
    
    Object getTarget();
    
    Method getMethod();
    
    Object[] getArgs();
    
    Object proceed() throws Throwable;
}
