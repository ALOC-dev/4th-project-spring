package dev.aloc.spring;

import java.util.Set;

/*
    1. 클래스패스에서 basePackage 이하 스캔
    2. @Component 애너테이션이 붙은 클래스들을 모두 찾는다.

    - 파일 시스템과 JAR 내부 양쪽 지원
    - 내부/익명/지역 클래스 제외
    - 인터페이스/애너테이션/열거형/추상클래스 제외
 */
public interface ComponentScanner {
    Set<Class<?>> scan(String basePackage);
}