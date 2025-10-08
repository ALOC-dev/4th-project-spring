package dev.aloc.spring;

import dev.aloc.spring.annotation.Autowired;
import dev.aloc.spring.annotation.Component;
import dev.aloc.spring.exception.ConstructorResolutionException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class InstantiationUtil {
    /**
     * 우선순위에 따라 BeanDefinition에 들어갈 최적의 생성자 하나를 찾는 메소드.
     * <p>
     * 우선순위는 @Autowired가 붙은 생성자 -> 단일 생성자 -> 파라미터가 가장 많은 생성자 순으로 찾기
     *
     * @param type
     * @return
     */
    public static Constructor<?> resolveConstructor(Class<?> type) {
        
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        // 생성자가 없을 경우
        if (ctors.length == 0) {
            throw new IllegalStateException(type.getName() + ": 생성자가 없습니다.");
        }
        
        // @Autowired가 붙은 생성자들 리스트에 저장
        List<Constructor<?>> autowiredCtors = Arrays.stream(ctors)
            .filter(ctor -> ctor.isAnnotationPresent(Autowired.class))
            .toList();
        
        // 1. 적어도 하나 저장되었을 경우
        if (!autowiredCtors.isEmpty()) {
            if (autowiredCtors.size() == 1) {
                return autowiredCtors.get(0);
            } else {
                throw new ConstructorResolutionException(
                    type.getName() + ": @Autowired가 지정된 생성자가 두 개 이상입니다."
                );
            }
        }
        
        // 2. 저장 하나도 안 되어 있을 때 생성자가 하나였을 경우
        if (ctors.length == 1) {
            return ctors[0];
        }
        
        // 3. 파라미터가 가장 많은 생성자
        int max = Arrays.stream(ctors).mapToInt(Constructor::getParameterCount).max().orElse(0);
        List<Constructor<?>> maxCtors = Arrays.stream(ctors)
            .filter(ctor -> ctor.getParameterCount() == max)
            .toList();
        if (maxCtors.size() == 1) {
            return maxCtors.get(0);
        }
        
        // 4. bean을 가장 많이 가진 생성자 -> 그래도 동률이면 오류
        int maxBeans = maxCtors.stream()
            .mapToInt(ctor -> (int) Arrays.stream(ctor.getParameterTypes())
                .filter(param -> param.isAnnotationPresent(Component.class)).count())
            .max().orElse(0);
        List<Constructor<?>> maxBeansCtors = maxCtors.stream()
            .filter(ctor -> Arrays.stream(ctor.getParameterTypes())
                .filter(param -> param.isAnnotationPresent(Component.class)).count() == maxBeans)
            .toList();
        if (maxBeansCtors.size() == 1) {
            return maxBeansCtors.get(0);
        }
        
        throw new ConstructorResolutionException(
            type.getName() + ": 의존성 주입에 사용할 생성자를 특정할 수 없습니다."
        );
        
    }
}
