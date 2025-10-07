package dev.aloc.spring;

import dev.aloc.spring.enums.CreationStatus;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanFactory implements BeanFactory {
    // Bean에 대한 메타정보를 담아두는 Map
    private final Map<Class<?>, BeanDefinition> defs = new ConcurrentHashMap<>();
    // 실제 Bean 싱글턴을 담는 Map
    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    
    // @Component 붙은 클래스를 받아서 defs와 beans에 차례로 등록해주는 메소드
    @Override
    public void registerBeans(Set<Class<?>> classesToRegister) {
        /*
         * 일단 처음엔 @Component가 붙어있는 (+scope가 singleton인것까지 걸러주면 더 좋음)
           Class들만 받아서 전부 defs에 BeanDefinition객체 생성해서 넣어주기
         * "defs에 있으면 다 bean으로 만들어야 하는 것"으로 간주할 수 있음
         */
        for (Class<?> clazz : classesToRegister) {
            defs.put(clazz, new BeanDefinition(clazz));
        }
        
        for (Class<?> clazz : classesToRegister) {
            if (!beans.containsKey(clazz)) {
                getBean(clazz); // 여기서 Bean 생성 및 맵에 넣어주는것까지 다 함
            }
        }
    }
    
    /*
     * Bean 생성 과정에서 이미 생성되었는지 확인하기 위해 사용하는 메소드
     * 생성되지 않은 Bean은 createBean() 호출을 통해 생성,
       createBean() 내부에서 다시 getBean()을 호출하기 때문에 재귀호출 구조가 됨
     *
     * 순환참조 발생 시 에러처리 필요 (StackOverflowError 등)
     */
    @Override
    public Object getBean(Class<?> beanType) {
        Object bean = beans.get(beanType);
        if (bean != null) {
            return bean;
        }
        
        BeanDefinition def = defs.get(beanType);
        if (def != null) {
            // 아직 생성 중인 빈을 필요로 한다 == 순환참조
            // StackOverflow 발생 전 미리 끊어버리기
            if (def.getStatus() == CreationStatus.CREATING) {
                throw new RuntimeException("순환 참조 발생!");
            }
            return createBean(def);
        }
        
        throw new RuntimeException(beanType.getName() + " 타입의 Bean을 찾을 수 없습니다.");
    }
    
    /*
     * 생성되지 않은 Bean을 만들어주는 메소드
     * 클래스의 메타정보(생성자 종류나 매개변수 등)를 받아 매개변수의 타입에 따라 또다른 bean(getBean 재귀호출),
       원시자료형이면 디폴트값, 또는 null(아직 임시 조치중)을 구분해서 넣어줌
     */
    @Override
    public Object createBean(BeanDefinition def) {
        try {
            def.setStatus(CreationStatus.CREATING); // '생성 중'으로 상태 전환
            
            Constructor<?> ctor = def.getInjectionCtor();
            Class<?>[] paramTypes = def.getParamTypes();
            Object[] args = new Object[paramTypes.length];
            
            for (int i = 0; i < args.length; i++) {
                Class<?> type = paramTypes[i];
                
                if (defs.containsKey(type)) {
                    args[i] = getBean(type);
                    
                } else if (type.isInterface()) {
                    // 혹시 필요할까봐 남기기
                    args[i] = null;
                    
                } else if (type.isPrimitive()) {
                    args[i] = defaultPrimitive(type);
                } else {
                    args[i] = null;
                }
            }
            
            Object bean = ctor.newInstance(args);
            if (!beans.containsKey(def.getBeanType())) {
                beans.put(def.getBeanType(), bean);
            }
            def.setStatus(CreationStatus.CREATED); // '생성 완료'로 전환
            return bean;
            
        } catch (Exception e) {
            throw new RuntimeException("생성자 주입 에러 발생: " + e.getMessage());
        }
    }
    
    /*
     * 의존성 추가 및 빈등록이 모두 끝난 뒤 외부에서 검사 용도로만 사용
     * getBean()과 달리 beans 목록에 없으면 defs에도 없는 상태가 됨, 생성 과정 없이 바로 에러 발생시킴
     */
    @Override
    public <T> T getExistingBean(Class<T> beanType) {
        // Key(클래스 타입)를 이용해 Value(인스턴스)를 조회
        Object bean = beans.get(beanType);
        
        // 찾는 Bean이 등록되지 않았을 경우 예외
        if (bean == null) {
            throw new RuntimeException(beanType.getName() + " 타입의 Bean을 찾을 수 없습니다.");
        }
        
        // 객체를 T로 형변환 한 뒤 반환
        return beanType.cast(bean);
    }
    
    // 원시자료형일 경우 디폴트값 객체로 만들어주는 헬퍼메소드
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
        throw new IllegalArgumentException("Unsupported primitive: " + t);
    }
}
