package dev.aloc.spring;

import java.util.Set;

/*
 * ComponentScanner의 책임:
 * 특정 패키지(basePackage)를 받아서
 * 그 안에 @Component 애너테이션이 붙은 클래스들을 모두 찾아
 * Set<Class<?>> 형태로 반환한다.
*/
public interface ComponentScanner {
    Set<Class<?>> scan(String basePackage);
}