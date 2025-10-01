package dev.aloc.spring.mybeans;

import dev.aloc.spring.annotation.Component;
import java.util.Arrays;

@Component
public class MyRepository {
    
    int testno;
    
    // 생성자 여러 개 만들기 (테스트 용도)
    public MyRepository(int testno) {
        this.testno = testno;
    }
    
    public MyRepository() {
        this.testno = 1;
    }
    
    // test
    public void printInfo() throws Exception {
        System.out.println(MyRepository.class.getName());
        System.out.println(Arrays.toString(MyRepository.class.getDeclaredConstructors()));
        System.out.println(Arrays.toString(
            MyRepository.class.getDeclaredConstructor(int.class).getParameterTypes()));
        System.out.println("testno: " + testno);
        System.out.println("-----------------------------------------------------------");
    }
}
