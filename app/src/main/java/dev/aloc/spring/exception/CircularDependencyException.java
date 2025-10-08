package dev.aloc.spring.exception;

/**
 * Bean 간의 순환 참조가 일어날 때 예외를 발생시킨다.
 */
public class CircularDependencyException extends RuntimeException {
    
    // 전달받은 메시지를 통해 순환 참조 오류 발생을 표시한다.
    public CircularDependencyException(String message) {
        super(message);
    }
}
