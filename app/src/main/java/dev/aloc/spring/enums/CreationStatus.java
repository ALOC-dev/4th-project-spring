package dev.aloc.spring.enums;

/**
 * Bean의 생성 상태를 표시하기 위해 사용된다.
 */
public enum CreationStatus {
    // Bean이 생성되어 맵에 등록된 상태이다.
    CREATED,
    // BeanDefinition을 찾았고 파라미터에 의존성 주입이 진행되고 있는 상태이다.
    CREATING,
    // 아직 BeanDefinition에 접근이 안 되어 Bean 생성이 시작되지 않은 상태이다.
    NOT_CREATED;
}
