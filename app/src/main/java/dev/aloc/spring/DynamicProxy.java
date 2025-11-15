package dev.aloc.spring;

import java.lang.reflect.Proxy;

// 객체 메서드가 호출될 때, 앞뒤로 내가 원하는 동작 넣는 기능

// 실행 예제
public class DynamicProxy {
    public static void main(String[] args) {
        // 객체 생성
        Hello target = new HelloImpl();

        // Dynamic Proxy 생성
        Hello proxyInstance = (Hello) Proxy.newProxyInstance(
                Hello.class.getClassLoader(),        // 클래스 로더
                new Class<?>[]{Hello.class},         // 프록시가 구현할 인터페이스 목록
                new LoggingInvocationHandler(target) // InvocationHandler
        );

        // 프록시 객체를 통해 메서드 호출
        String result = proxyInstance.sayHello("Chaewoo");

        System.out.println("결과: " + result);
    }
}