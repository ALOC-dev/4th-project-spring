package dev.aloc.spring;

import dev.aloc.spring.enums.CreationStatus;
import dev.aloc.spring.enums.Scope;
import dev.aloc.spring.exception.BeanCreationException;
import dev.aloc.spring.exception.ConstructorResolutionException;
import java.lang.reflect.Constructor;
import java.util.Objects;

public class BeanDefinition {
    
    private Class<?> beanType;
    private Scope scope = Scope.SINGLETON; // 스코프 (디폴트: 싱글턴)
    private CreationStatus status = CreationStatus.NOT_CREATED;
    
    private final Constructor<?> injectionCtor; // reflection에서 사용할 하나의 생성자
    
    // 생성자의 모든 매개변수 목록
    // List 말고 그냥 배열이 불변성 목적에도 더 맞고 인덱스 접근이 빨라서 좋다고 하네요..
    private final Class<?>[] paramTypes;
    
    // 생성자
    public BeanDefinition(Class<?> beanType) {
        this.beanType = Objects.requireNonNull(beanType);
        // Autowired 어노테이션 있는지 체크 (코드 추가해야함)
        try {
            this.injectionCtor = InstantiationUtil.resolveConstructor(beanType);
            injectionCtor.setAccessible(true);
            this.paramTypes = injectionCtor.getParameterTypes();
        } catch (ConstructorResolutionException e) {
            throw new BeanCreationException(beanType.getName() + "의 BeanDefinition 생성 실패: 주입할 생성자를 결정할 수 없습니다.", e);
        } catch (Exception e) {
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
