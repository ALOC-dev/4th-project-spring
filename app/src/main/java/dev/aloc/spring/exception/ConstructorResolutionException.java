package dev.aloc.spring.exception;

/**
 * BeanDefinition에 들어갈 하나의 생성자를 찾지 못했을 때 예외를 발생시킨다.
 */
public class ConstructorResolutionException extends RuntimeException {
    
    // 전달받은 에러 메시지로 생성자 지정 오류를 표시한다.
    public ConstructorResolutionException(String message) {
        super(message);
    }
}
