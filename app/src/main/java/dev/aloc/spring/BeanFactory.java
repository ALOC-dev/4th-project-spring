package dev.aloc.spring;

import java.util.Set;

/*
 * BeanFactory의 책임:
 * 1. ComponentScanner가 찾아온 클래스 목록을 받아서 Bean으로 등록한다. (registerBeans) 근데 이제 Map으로 저장하기
 * 2. 외부에서 특정 타입의 Bean을 달라고 요청하면, 찾아서 내어준다. (getBean)
*/
public interface BeanFactory {
    // 1. Bean 등록 기능
    void registerBeans(Set<Class<?>> classesToRegister);

    // 2. Bean 조회 기능
    <T> T getBean(Class<T> beanType);
}