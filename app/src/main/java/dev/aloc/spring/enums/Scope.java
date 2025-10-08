package dev.aloc.spring.enums;

/**
 * Bean을 만들 때 스코프 종류를 표시하기 위해 사용된다.
 */
public enum Scope {
    // 해당 타입의 Bean 객체를 하나만 만들어 사용한다. (디폴트)
    SINGLETON,
    // 요청받을 때마다 새로운 Bean 객체를 만들어 사용한다.
    PROTOTYPE
}
