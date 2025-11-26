package dev.aloc.spring.testcomponents.calculator;

import dev.aloc.spring.annotation.Component;

// 컴포넌트 스캔 -> 빈 등록 -> AOP 적용
@Component
public class CalculatorImpl implements Calculator {

    @Override
    public int add(int a, int b) {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return a + b;
    }
}