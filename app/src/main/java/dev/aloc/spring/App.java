package dev.aloc.spring;

import dev.aloc.spring.mybeans.MyRepository;
import dev.aloc.spring.mybeans.MyService;
import java.util.Set;

public class App {
//    public String getGreeting() {
//        return "Hello World!";
//    }
    
    public static void main(String[] args) {
        
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
