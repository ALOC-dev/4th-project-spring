package dev.aloc.spring.exception;

/**
 * Bean 생성이 되지 않았을 때 예외를 발생시킨다.
 */
public class NoSuchBeanDefinitionException extends RuntimeException {
    
    // 전달받은 에러 메시지를 통해 Bean이 없다는 오류 내용을 표시한다.
    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }
}
