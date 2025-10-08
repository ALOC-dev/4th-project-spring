package dev.aloc.spring;

import java.util.Set;

/**
 * Bean 등록과 조회를 위한 인터페이스이다.
 * <p>
 * BeanFactory의 책임:
 * <p>
 * 1. ComponentScanner가 찾아온 클래스 목록을 받아서 Bean으로 등록한다. (registerBeans) 근데 이제 Map으로 저장하기
 * <p>
 * 2. 내/외부에서 특정 타입의 Bean을 달라고 요청하면 찾아서 내어준다. (getBean, getExistingBean)
 */
public interface BeanFactory {
    
    // 1. Bean 등록 기능
    void registerBeans(Set<Class<?>> classesToRegister);
    
    // 2. Bean 조회 및 없으면 생성해주는 기능 (내부에서 사용)
    Object getBean(Class<?> beanType);
    
    // 3. 아직 등록되지 않은 Bean을 생성하는 기능
    Object createBean(BeanDefinition def);
    
    // 4. Bean 조회 기능 (외부에서 사용)
    <T> T getExistingBean(Class<T> beanType);
}