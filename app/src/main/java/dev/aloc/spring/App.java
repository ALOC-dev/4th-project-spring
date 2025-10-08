package dev.aloc.spring;

import dev.aloc.spring.mybeans.MyRepository;
import dev.aloc.spring.mybeans.MyService;
import java.util.Set;

public class App {
    
    /**
     * MyService와 MyRepository로 수동으로 bean을 만들고, 잘 만들어졌는지 결과를 출력한다.
     *
     * @param args
     */
    public static void main(String[] args) {
        
        // 일단 수동으로 인자에 넣기
        Set<Class<?>> beans = Set.of(MyService.class, MyRepository.class);
        
        SimpleBeanFactory sbf = new SimpleBeanFactory();
        sbf.registerBeans(beans);
        
        try {
            sbf.getExistingBean(MyService.class).printInfo();
            sbf.getExistingBean(MyRepository.class).printInfo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
