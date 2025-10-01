package dev.aloc.spring.mybeans;

import dev.aloc.spring.annotation.Autowired;
import dev.aloc.spring.annotation.Component;
import java.util.Arrays;

@Component
public class MyService {
    private final MyRepository repository;
    private String testString;
    
    @Autowired
    public MyService(MyRepository repository, String testString) {
        this.repository = repository;
        this.testString = testString;
    }
    
    // test
    public void printInfo() throws Exception {
        System.out.println(MyService.class.getName());
        System.out.println(Arrays.toString(MyService.class.getDeclaredConstructors()));
        System.out.println(Arrays.toString(
            MyService.class.getDeclaredConstructor(MyRepository.class, String.class)
                .getParameterTypes()));
        System.out.println("-----------------------------------------------------------");
    }
}
