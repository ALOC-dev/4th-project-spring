package dev.aloc.spring.mybeans;

import dev.aloc.spring.annotation.Autowired;
import dev.aloc.spring.annotation.Component;
import java.util.Arrays;

/**
 * Bean 생성 및 의존성 주입 테스트를 위한 임시 서비스 클래스이다.
 */
@Component
public class MyService {
    // Bean에 해당하는 MyRepository를 필드로 가진다.
    private final MyRepository repository;
    // 테스트를 위해 만든 String 타입의 필드
    private String testString;
    
    /**
     * 의존성 주입 테스트를 위한 @Autowired 생성자.
     * <p>
     * Autowired가 붙은 유일한 생성자이기 때문에 InstantiationUtil에서는 이 생성자를 정상적으로 선택해야 한다.
     *
     * @param repository SimpleBeanFactory에서 Bean으로 주입해주어야 하는 클래스.
     * @param testString SimpleBeanFactory에서 null로 주입해주어야 하는 클래스.
     */
    @Autowired
    public MyService(MyRepository repository, String testString) {
        this.repository = repository;
        this.testString = testString;
    }
    
    /**
     * Bean으로 잘 만들어졌는지 테스트를 위해 이 클래스의 정보를 출력한다.
     *
     * @throws Exception reflection 메소드를 사용할 때 발생할 수 있는 예외를 main으로 넘긴다.
     */
    public void printInfo() throws Exception {
        // 패키지+클래스명 출력
        System.out.println(MyService.class.getName());
        // 생성자 목록 출력
        System.out.println(Arrays.toString(MyService.class.getDeclaredConstructors()));
        // Autowired 생성자 (SimpleBeanFactory에서 선택된 생성자)의 매개변수 목록 출력
        System.out.println(Arrays.toString(
            MyService.class.getDeclaredConstructor(MyRepository.class, String.class)
                .getParameterTypes()));
        System.out.println("-----------------------------------------------------------");
    }
}
