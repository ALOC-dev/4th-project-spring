package dev.aloc.spring.mybeans;

import dev.aloc.spring.annotation.Component;
import java.util.Arrays;

/**
 * Bean 생성 및 원시자료형 주입 테스트를 위한 임시 리포지토리 클래스이다.
 */
@Component
public class MyRepository {
    
    // 원시자료형 주입이 잘 되는지 확인하기 위한 int 필드.
    int testno;
    
    /**
     * 매개변수가 더 많은 생성자 (SimpleBeanFactory가 선택해야 할 생성자).
     *
     * @param testno SimpleBeanFactory가 testno에 기본값을 잘 주입했다면 0이 되어야 한다.
     */
    public MyRepository(int testno) {
        this.testno = testno;
    }
    
    /**
     * 매개변수가 더 적은 생성자 (SimpleBeanFactory가 선택하면 안 되는 생성자).
     */
    public MyRepository() {
        this.testno = 1;
    }
    
    /**
     * Bean으로 잘 만들어졌는지 테스트를 위해 이 클래스의 정보를 출력한다.
     *
     * @throws Exception reflection 메소드를 사용할 때 발생할 수 있는 예외를 main으로 넘긴다.
     */
    public void printInfo() throws Exception {
        // 패키지+클래스명 출력
        System.out.println(MyRepository.class.getName());
        // 생성자 목록 출력
        System.out.println(Arrays.toString(MyRepository.class.getDeclaredConstructors()));
        // int를 매개변수로 가지는 생성자 (SimpleBeanFactory에서 선택된 생성자)의 매개변수 목록 출력
        System.out.println(Arrays.toString(
            MyRepository.class.getDeclaredConstructor(int.class).getParameterTypes()));
        /*
         * SimpleBeanFactory에서 원시자료형을 잘 주입해 주었는지 테스트하기 위한 출력.
         * testno의 값은 첫 번째 생성자를 제대로 선택했다면 0, 두 번째 생성자를 선택했다면(잘못 선택함) 1이 된다.
         */
        System.out.println("testno: " + testno);
        System.out.println("-----------------------------------------------------------");
    }
}
