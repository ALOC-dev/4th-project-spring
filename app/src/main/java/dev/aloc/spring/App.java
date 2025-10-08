package dev.aloc.spring;

import dev.aloc.spring.mybeans.MyRepository;
import dev.aloc.spring.mybeans.MyService;
import java.util.Set;

public class App {
    /**
     * MyService와 MyRepository로 수동으로 bean을 만들고, 잘 만들어졌는지 결과를 출력한다. 빌드 시작하는 즉시 자동으로 bean을 만들어주는 기능은
     * 추후 ComponentScanner 클래스 완성되면 구현 예정
     *
     * @param args .
     */
    public static void main(String[] args) {
        
        // 일단 수동으로 MyService, MyRepository 클래스들을 매개변수로 넣어 Bean으로 만들어준다.
        ComponentScanner scanner = new ClasspathComponentScanner();
        Set<Class<?>> beans = scanner.scan("dev.aloc.spring");
        System.out.println("Scanned components: " + beans);
        
        SimpleBeanFactory sbf = new SimpleBeanFactory();
        sbf.registerBeans(beans);
        
        try {
            // Bean이 잘 만들어졌는지, 안에 매개변수들이 제대로 주입되었는지 테스트
            sbf.getExistingBean(MyService.class).printInfo();
            sbf.getExistingBean(MyRepository.class).printInfo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
