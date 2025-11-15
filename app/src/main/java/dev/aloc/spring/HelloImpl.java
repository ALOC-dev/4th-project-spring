package dev.aloc.spring;

// Hello 구현체
class HelloImpl implements Hello {
    @Override
    public String sayHello(String name) {
        System.out.println("HelloImpl.sayHello 실행 중...");
        return "Hello, " + name;
    }
}
