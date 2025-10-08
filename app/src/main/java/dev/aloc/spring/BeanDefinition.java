package dev.aloc.spring;

import dev.aloc.spring.enums.CreationStatus;
import dev.aloc.spring.enums.Scope;
import dev.aloc.spring.exception.BeanCreationException;
import dev.aloc.spring.exception.ConstructorResolutionException;
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Bean으로 등록할 클래스의 메타 정보를 저장한다. (스코프, 생성 상태, 생성자 및 파라미터 정보 등)
 */
public class BeanDefinition {
    
    // Bean의 타입
    private Class<?> beanType;
    // Bean의 스코프 (디폴트: 싱글턴)
    private Scope scope = Scope.SINGLETON;
    // Bean의 생성 상태 (디폴트: NOT_CREATED (아직 생성되지 않음))
    private CreationStatus status = CreationStatus.NOT_CREATED;
    
    // reflection에서 사용할 하나의 생성자
    private final Constructor<?> injectionCtor;
    
    // 생성자의 모든 매개변수 목록
    private final Class<?>[] paramTypes;
    
    /**
     * BeanDefinition을 생성하면서 beanType 클래스의 생성자 및 매개변수들을 찾아 필드로 저장한다.
     *
     * @param beanType BeanDefinition을 정의할 클래스 (&#64;Component 붙어있음)
     */
    public BeanDefinition(Class<?> beanType) {
        this.beanType = Objects.requireNonNull(beanType);
        try {
            // 생성자 지정 우선순위에 따라 하나의 생성자를 찾고, 매개변수 목록을 구한다.
            this.injectionCtor = InstantiationUtil.resolveConstructor(beanType);
            injectionCtor.setAccessible(true);
            this.paramTypes = injectionCtor.getParameterTypes();
            
        } catch (ConstructorResolutionException e) {
            // 생성자 지정에서 오류가 발생했을 경우 에러메시지를 표시한다.
            throw new BeanCreationException(
                beanType.getName() + "의 BeanDefinition 생성 실패: 주입할 생성자를 결정할 수 없습니다.", e);
            
        } catch (Exception e) {
            // 기타 오류가 발생했을 경우 에러메시지와 발생 원인을 표시한다.
            throw new BeanCreationException(beanType.getName() + "의 BeanDefinition 생성 실패", e);
        }
    }
    
    // getters/setters
    public Class<?> getBeanType() {
        return beanType;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public CreationStatus getStatus() {
        return status;
    }
    
    public Class<?>[] getParamTypes() {
        return paramTypes;
    }
    
    public Constructor<?> getInjectionCtor() {
        return injectionCtor;
    }
    
    public void setBeanClass(Class<?> beanType) {
        this.beanType = beanType;
    }
    
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    
    public void setStatus(CreationStatus status) {
        this.status = status;
    }
}
