package dev.aloc.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {
    String value(); // 포인트컷을 넣어 주면 이 어노테이션을 붙인 메서드 전후로 Aspect가 실행됨
}
