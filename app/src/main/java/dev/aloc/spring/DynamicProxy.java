import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 객체 메서드가 호출될 때, 앞뒤로 내가 원하는 동작 넣는 기능

// Hello 인터페이스
interface Hello {
    String sayHello(String name);
}

// Hello 구현체
class HelloImpl implements Hello {
    @Override
    public String sayHello(String name) {
        System.out.println("HelloImpl.sayHello 실행 중...");
        return "Hello, " + name;
    }
}

// InvocationHandler 구현체 (공통 로직: Before / After 로그)
class LoggingInvocationHandler implements InvocationHandler {

    private final Object target; // 실제 객체

    public LoggingInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Before 출력
        System.out.println("[Before] 메서드 이름: " + method.getName());

        // 원본 메서드 호출
        Object result = method.invoke(target, args);

        // After 출력
        System.out.println("[After] 메서드 이름: " + method.getName());

        return result;
    }
}

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