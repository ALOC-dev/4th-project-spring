package dev.aloc.spring.aop;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectiveMethodInvocation implements MethodInvocation {
    
    private final Object target;
    private final Method method;
    private final Object[] args;
    private final List<MethodInterceptor> interceptors; // 체인으로 실행할 인터셉터 목록
    
    private int itcIndex = -1; // 인터셉터 리스트에서의 현재 인덱스
    
    public ReflectiveMethodInvocation(
        Object target,
        Method method,
        Object[] args,
        List<MethodInterceptor> interceptors
    ) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.interceptors = interceptors;
    }
    
    @Override
    public Object getTarget() {
        return target;
    }
    
    @Override
    public Method getMethod() {
        return method;
    }
    
    @Override
    public Object[] getArgs() {
        return args;
    }
    
    @Override
    public Object proceed() throws Throwable {
        itcIndex++;
        // 모든 인터셉터를 실행했으면 실제 메소드 호출
        if (itcIndex == interceptors.size()) {
            return method.invoke(target, args);
        }
        
        MethodInterceptor interceptor = interceptors.get(itcIndex);
        return interceptor.invoke(this);
    }
}
