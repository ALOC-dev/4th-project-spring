package dev.aloc.spring;

import dev.aloc.spring.testcomponents.MyRepository;
import dev.aloc.spring.testcomponents.MyService;
import java.util.Set;

/**
 * 빌드가 완료되면 ComponentScanner로 @Component가 붙은 클래스를 모두 찾아낸 뒤, BeanFactory에서 Bean으로 등록해준다.
 */
public class App {
    public static void main(String[] args) {
        
        // scanner로 @Component가 붙은 클래스를 자동으로 찾은 뒤 결과 출력.
        ComponentScanner scanner = new ClasspathComponentScanner();
        Set<Class<?>> beans = scanner.scan("dev.aloc.spring");
        System.out.println("Scanned components: " + beans);
        
        // BeanFactory에서 beans로 받은 클래스들을 모두 Bean으로 생성 + 의존성 주입
        BeanFactory bf = new SimpleBeanFactory();
        bf.registerBeans(beans);
        
        try {
            // Bean들이 잘 만들어졌는지, 안에 매개변수들이 제대로 주입되었는지 테스트
            bf.getExistingBean(MyService.class).printInfo();
            bf.getExistingBean(MyRepository.class).printInfo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
