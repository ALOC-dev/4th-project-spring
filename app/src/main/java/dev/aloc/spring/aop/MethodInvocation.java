package dev.aloc.spring.aop;

import java.lang.reflect.Method;

public interface MethodInvocation {
    // 호출 대상 객체 정보
    Object getTarget();
    // 호출 대상 객체의 메서드 정보
    Method getMethod();
    // 호출 시 전달된 인자들
    Object[] getArgs();
    // 호출 대상 객체 메서드 실행
    Object proceed() throws Throwable;
}
