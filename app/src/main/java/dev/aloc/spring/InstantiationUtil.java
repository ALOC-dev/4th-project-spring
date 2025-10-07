package dev.aloc.spring;

import dev.aloc.spring.annotation.Autowired;
import dev.aloc.spring.annotation.Component;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 * 객체(Bean)를 생성할 때 어떤 생성자를 사용해야 할지 결정해주는 '생성자 선택 전문가' 클래스이다.
 */
public final class InstantiationUtil {

    // new InstantiationUtil() 로 객체를 생성하지 못하도록 생성자를 private으로 선언한다.
    private InstantiationUtil() {}

    /**
     * 주어진 클래스 타입(type)에 대해 사용할 최적의 생성자 하나를 찾아 반환한다.
     * 스프링의 생성자 선택 규칙과 유사한 우선순위를 따른다.
     */
    public static Constructor<?> resolveConstructor(Class<?> type) {
        // 1. 먼저, 클래스에 선언된 모든 생성자를 가져온다.
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        if (ctors.length == 0) {
            // 생성자가 하나도 없으면 객체를 만들 수 없으므로 에러를 발생시킨다.
            throw new IllegalStateException(type.getName() + ": No declared constructors");
        }

        // --- 규칙 1순위: @Autowired 어노테이션이 붙은 생성자를 찾는다. ---
        Constructor<?> autowired = pickAutowiredConstructorOrNull(ctors, type);
        if (autowired != null) {
            // @Autowired가 붙은 생성자를 찾았다면, 바로 반환한다.
            return autowired;
        }

        // --- 규칙 2순위: 생성자가 딱 하나뿐인 경우를 확인한다.
        if (ctors.length == 1) {
            // 생성자가 하나뿐이라면 선택의 여지가 없으므로, 그 생성자를 반환한다.
            return ctors[0];
        }

        // --- 규칙 3순위: 파라미터 개수가 가장 많은 생성자를 찾는다. ---
        List<Constructor<?>> maxParamCtors = pickMaxParameterConstructors(ctors);
        if (maxParamCtors.size() == 1) {
            // 파라미터 개수가 가장 많은 생성자가 유일하다면, 그것을 선택하여 반환한다.
            return maxParamCtors.get(0);
        }

        // --- 규칙 4순위: 'Bean' 타입 파라미터가 가장 많은 생성자를 찾는다. ---
        List<Constructor<?>> mostBeanParamCtors = pickMostBeanParamConstructors(maxParamCtors);
        if (mostBeanParamCtors.size() == 1) {
            // 후보들 중에서 @Component가 붙은 '다른 부품(Bean)'을 가장 많이 필요로 하는 생성자를 반환한다.
            return mostBeanParamCtors.get(0);
        }

        // 위 모든 규칙으로도 하나의 생성자를 특정할 수 없다면, 모호하다는 에러를 발생시킨다.
        throw new IllegalStateException(type.getName() + ": Ambiguous constructors");
    }

    /**
     * 여러 생성자 중에서 @Autowired 어노테이션이 붙은 것을 찾는다.
     * @param ctors 검사할 생성자 배열
     * @param owner 클래스 타입 (에러 메시지 표기용)
     * @return @Autowired가 붙은 생성자, 없으면 null 반환
     */
    private static Constructor<?> pickAutowiredConstructorOrNull(Constructor<?>[] ctors, Class<?> owner) {
        // 스트림을 이용해 @Autowired 어노테이션이 존재하는 생성자만 필터링해서 리스트로 만든다.
        List<Constructor<?>> autowired = Arrays.stream(ctors)
                .filter(c -> c.isAnnotationPresent(Autowired.class))
                .toList();

        // @Autowired가 붙은 생성자가 없으면, null을 반환하여 다음 규칙으로 넘어가게 한다.
        if (autowired.isEmpty()) return null;

        // @Autowired를 두 개 이상 붙이는 것은 규칙 위반이므로, 에러를 발생시킨다.
        if (autowired.size() > 1) {
            throw new IllegalStateException(owner.getName() + ": More than one @Autowired constructor");
        }

        // @Autowired가 정확히 하나일 경우, 그 생성자를 반환한다.
        return autowired.get(0);
    }

    /**
     * 여러 생성자 중에서 파라미터 개수가 가장 많은 생성자들을 찾는다.
     * @param ctors 검사할 생성자 배열
     * @return 파라미터 개수가 최대인 생성자들의 리스트
     */
    private static List<Constructor<?>> pickMaxParameterConstructors(Constructor<?>[] ctors) {
        // 1. 먼저 모든 생성자들 중에서 파라미터 개수의 최댓값을 찾는다.
        int max = Arrays.stream(ctors).mapToInt(Constructor::getParameterCount).max().orElse(0);
        // 2. 그 최댓값과 파라미터 개수가 동일한 생성자들만 필터링하여 리스트로 반환한다.
        return Arrays.stream(ctors)
                .filter(c -> c.getParameterCount() == max)
                .toList();
    }

    /**
     * 후보 생성자들 중에서 '@Component' 타입의 파라미터를 가장 많이 가진 생성자들을 찾는다.
     * @param ctors 검사할 생성자 리스트 (이미 파라미터 개수가 최댓값인 후보들)
     * @return 'Bean' 타입 파라미터 개수가 최대인 생성자들의 리스트
     */
    private static List<Constructor<?>> pickMostBeanParamConstructors(List<Constructor<?>> ctors) {
        // 1. 후보 생성자들 중에서 'Bean' 타입 파라미터 개수의 최댓값을 계산한다.
        int maxBeans = ctors.stream()
                .mapToInt(c -> (int) Arrays.stream(c.getParameterTypes())
                        .filter(InstantiationUtil::isComponentType).count()) // isComponentType 메서드로 Bean인지 판별
                .max().orElse(0);

        // 2. 그 최댓값과 'Bean' 파라미터 개수가 동일한 생성자들만 필터링하여 리스트로 반환한다.
        return ctors.stream()
                .filter(c -> Arrays.stream(c.getParameterTypes())
                        .filter(InstantiationUtil::isComponentType).count() == maxBeans)
                .toList();
    }

    /**
     * 주어진 클래스 타입이 @Component 어노테이션을 가지고 있는지 확인한다.
     * @param type 검사할 클래스 타입
     * @return @Component가 붙어있으면 true, 아니면 false
     */
    private static boolean isComponentType(Class<?> type) {
        // 이 메서드를 통해 파라미터가 '일반 값'인지, 아니면 프레임워크가 관리하는 '다른 부품(Bean)'인지 구별한다.
        return type.isAnnotationPresent(Component.class);
    }
}
