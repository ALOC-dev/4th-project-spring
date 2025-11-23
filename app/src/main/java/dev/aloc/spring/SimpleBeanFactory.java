package dev.aloc.spring;

import dev.aloc.spring.aop.TimeLoggingAspect;
import dev.aloc.spring.enums.CreationStatus;
import dev.aloc.spring.exception.BeanCreationException;
import dev.aloc.spring.exception.CircularDependencyException;
import dev.aloc.spring.exception.NoSuchBeanDefinitionException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#64;Component 어노테이션이 붙은 클래스들을 Bean으로 만들기 위한 클래스이다.
 */
public class SimpleBeanFactory implements BeanFactory {
    // Bean에 대한 메타정보를 담아두는 Map
    private final Map<Class<?>, BeanDefinition> defs = new ConcurrentHashMap<>();
    // 실제 Bean 싱글턴을 담는 Map
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    
    /**
     * &#64;Component 어노테이션이 붙은 클래스들을 받아서 defs와 beans에 차례로 등록해준다. 처음에 모든 @Component 클래스에 대해
     * BeanDefinition을 생성한다. 이를 통해 "defs 안에 있으면 반드시 bean으로 만들어야 하는 것"으로 간주할 수 있다.
     *
     * @param classesToRegister Component 어노테이션이 붙은 클래스들의 집합.
     */
    @Override
    public void registerBeans(Set<Class<?>> classesToRegister) {
        // Set 안의 모든 클래스들에 대해 BeanDefinition 만들어 두기.
        for (Class<?> clazz : classesToRegister) {
            defs.put(clazz, new BeanDefinition(clazz));
        }
        
        // 아직 Bean으로 등록되지 않은 클래스에 대해 Bean 생성 및 등록하기.
        for (Class<?> clazz : classesToRegister) {
            if (!beans.containsKey(clazz)) {
                getBean(clazz);
            }
        }
    }
    
    /**
     * 이미 등록된 Bean을 찾거나 없으면 새로 만들어 반환한다. 생성되지 않은 Bean은 createBean() 호출을 통해 생성, createBean() 내부에서 다시
     * getBean()을 호출하기 때문에 재귀호출 구조가 된다.
     *
     * @param beanType Bean으로 등록되었는지 확인하고자 하는 클래스.
     * @return 찾았거나 새로 생성한 Bean을 맵에 넣을 수 있도록 Object 형태로 되돌려 준다.
     */
    @Override
    public Object getBean(Class<?> beanType) {
        Object bean = beans.get(beanType);
        if (bean != null) {
            // 해당 클래스가 Bean으로 등록된 경우: 찾아서 되돌려 줌
            return bean;
        }
        
        BeanDefinition def = defs.get(beanType);
        if (def != null) {
            // StackOverflow 발생 전 미리 끊어준다.  아직 생성 중인 빈을 필요로 한다 == 순환참조를 의미
            if (def.getStatus() == CreationStatus.CREATING) {
                throw new CircularDependencyException("순환 참조가 발견되었습니다: " + beanType.getName());
            }
            // BeanDefinition만 있고 아직 Bean으로 등록되지는 않은 경우: 새로 만들어서 반환.
            return createBean(def);
        }
        
        // defs와 beans에 둘 다 들어있지 않은 것은 Bean으로 만들 필요가 없는 클래스이다.
        throw new NoSuchBeanDefinitionException(beanType.getName() + " 타입의 Bean 정의를 찾을 수 없습니다.");
    }
    
    /**
     * 아직 생성되지 않은 Bean을 만들기 위한 메소드이다. 클래스의 메타정보(생성자 종류나 매개변수 등)를 받아 매개변수의 타입에 따라 또다른 bean(getBean
     * 재귀호출), 원시자료형이면 디폴트값, 또는 null(아직 임시 조치중)을 구분해서 넣어준다.
     *
     * @param def Bean으로 생성하고자 하는 클래스의 BeanDefinition.
     * @return 생성된 Bean을 beans 맵에 들어갈 수 있도록 Object 형태로 되돌려 준다.
     */
    @Override
    public Object createBean(BeanDefinition def) {
        try {
            // '생성 중'으로 상태 전환
            def.setStatus(CreationStatus.CREATING);
            
            Constructor<?> ctor = def.getInjectionCtor();
            Class<?>[] paramTypes = def.getParamTypes();
            Object[] args = new Object[paramTypes.length];
            
            for (int i = 0; i < args.length; i++) {
                Class<?> type = paramTypes[i];
                
                if (defs.containsKey(type)) {
                    // &#64;Component가 붙은 클래스를 매개변수로 갖는 경우: getBean으로 찾거나 생성.
                    args[i] = getBean(type);
                    
                } else if (type.isInterface()) {
                    // 간혹 인터페이스를 Bean으로 만들려는 경우가 있기 때문에 인터페이스의 경우를 따로 분리함.
                    args[i] = null;
                    
                } else if (type.isPrimitive()) {
                    // 원시 자료형일 경우: 기본값을 반환하는 함수로 기본값 넣어주기
                    args[i] = defaultPrimitive(type);
                } else {
                    // 그 외 자료형은 일단 null로 넣어주기
                    args[i] = null;
                }
            }
            
            // 1. 원본 객체 생성
            Object originalBean = ctor.newInstance(args);
            
            Object beanToExpose = originalBean;
            
            // 일단 @Aspect 검사 대신 인터페이스가 있는 모든 Bean을 프록시 대상으로 해두었어요
            if (originalBean.getClass().getInterfaces().length > 0) {
                try {
                    // 프록시 생성
                    beanToExpose = java.lang.reflect.Proxy.newProxyInstance(
                        originalBean.getClass().getClassLoader(),
                        originalBean.getClass().getInterfaces(),
                        new dev.aloc.spring.aop.AspectProxyHandler(originalBean,
                            List.of(new TimeLoggingAspect()))
                    );
                    System.out.println("[AOP] 프록시 생성 완료: " + def.getBeanType().getName());
                } catch (Exception e) {
                    // 프록시 만들다가 실패하면 그냥 원본을 씁니다. (안전장치)
                    System.out.println("[AOP] 프록시 생성 실패 (원본 사용): " + e.getMessage());
                    beanToExpose = originalBean;
                }
            }
            
            if (!beans.containsKey(def.getBeanType())) {
                beans.put(def.getBeanType(), beanToExpose);
            }
            
            def.setStatus(CreationStatus.CREATED);
            return beanToExpose;
            
        } catch (NoSuchBeanDefinitionException e) {
            String message = "Bean '" + def.getBeanType().getName() + "' 생성 실패: 생성자 파라미터 타입인 '"
                + e.getMessage().split(" ")[0] // "dev.aloc.spring.MyRepository" 같은 부분
                + "' 타입의 Bean 정의를 찾을 수 없습니다.";
            throw new BeanCreationException(message, e);
            
        } catch (Exception e) {
            String message = "Bean '" + def.getBeanType().getName() + "' 생성 실패";
            throw new BeanCreationException(message, e);
        }
    }
    
    /**
     * 주어진 클래스가 Bean으로 잘 만들어졌는지 테스트한다. 의존성 추가 및 빈등록이 모두 끝난 뒤 외부에서 검사 용도로만 사용한다.
     *
     * @param beanType Bean으로 잘 생성되었는지 검사하고자 하는 클래스
     * @param <T>      beanType을 타입으로 가지는 실제 객체 반환을 위해 제네릭을 사용함.
     * @return Bean으로 생성되었을 경우 해당 객체를 반환.
     */
    @Override
    public <T> T getExistingBean(Class<T> beanType) {
        // Key(클래스 타입)를 이용해 Value(인스턴스)를 조회
        Object bean = beans.get(beanType);
        
        // 찾는 Bean이 등록되지 않았을 경우 예외
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(
                beanType.getName() + " 타입으로 등록된 Bean 인스턴스를 찾을 수 없습니다."
            );
        }
        
        // 객체를 T로 형변환 한 뒤 반환
        return beanType.cast(bean);
    }
    
    /**
     * createBean 메소드 내에서 원시자료형을 파라미터로 가지는 클래스의 경우 해당 타입의 디폴트값 객체를 반환한다.
     *
     * @param t 파라미터에 해당하는 원시자료형.
     * @return t 타입의 디폴트값을 Object 형태로 반환한다.
     */
    private Object defaultPrimitive(Class<?> t) {
        if (t == boolean.class) {
            return false;
        }
        if (t == char.class) {
            return '\0';
        }
        if (t == byte.class) {
            return (byte) 0;
        }
        if (t == short.class) {
            return (short) 0;
        }
        if (t == int.class) {
            return 0;
        }
        if (t == long.class) {
            return 0L;
        }
        if (t == float.class) {
            return 0f;
        }
        if (t == double.class) {
            return 0d;
        }
        throw new IllegalArgumentException("지원하지 않는 primitive 타입: " + t);
    }
}
