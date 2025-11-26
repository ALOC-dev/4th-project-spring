package dev.aloc.spring.testcomponents.calculator;

// AOP 프록시가 감쌀 타객 객체가 반드시 인터페이스 기반이어야 함.

public interface Calculator {
    int add(int a, int b);
}