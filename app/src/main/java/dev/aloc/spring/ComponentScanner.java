package dev.aloc.spring;

import java.util.Set;

/**
 * 클래스 중 @Component가 붙은 것들을 찾기 위한 인터페이스이다. ComponentScanner의 책임: 1. 클래스패스에서 basePackage 이하 스캔 2.
 * &#64;Component 애너테이션이 붙은 클래스들을 모두 찾는다.
 * <p>
 * - 파일 시스템과 JAR 내부 양쪽 지원
 * <p>
 * - 내부/익명/지역 클래스 제외
 * <p>
 * - 인터페이스/애너테이션/열거형/추상클래스 제외
 */
public interface ComponentScanner {
    Set<Class<?>> scan(String basePackage);
}