package dev.aloc.spring;

import dev.aloc.spring.annotation.Autowired;
import dev.aloc.spring.annotation.Component;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class InstantiationUtil {
    // @Autowired 붙은 생성자를 찾아주는 메소드
    // @Autowired가 붙은 생성자 -> 단일 생성자 -> 파라미터가 가장 많은 생성자 순으로 찾기
    public static Constructor<?> resolveConstructor(Class<?> type) {
        
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        // 생성자가 없을 경우
        if (ctors.length == 0) {
            throw new IllegalStateException(type.getName() + ": There is no constructor");
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
                throw new IllegalStateException(
                    type.getName() + ": There are more than 1 autowired constructors");
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
        
        throw new IllegalStateException(
            type.getName() + ": Ambiguous constructors with max parameter count");
    }
}
