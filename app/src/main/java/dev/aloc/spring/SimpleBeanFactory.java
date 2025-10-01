package dev.aloc.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleBeanFactory implements BeanFactory {
  // Bean들을 담아둘 해시 더미들
  // key : Bean의 클래스 타입
  // value : 만들어진 Bean 객체 인스턴스
  private final Map<Class<?>, Object> beans = new HashMap<>();

  public void registerBeans(Set<Class<?>> classesToRegister) {
    for (Class<?> clazz : classesToRegister) {
      try {
        Object beanInstance = clazz.getDeclaredConstructor().newInstance(); // Reflection API - 인스턴스 생성
        beans.put(clazz, beanInstance); // Map에 저장
      } catch (Exception e) {
        throw new RuntimeException("Bean 인스턴스 생성 실패: " + clazz.getName(), e);
      }
    }
  }

  public <T> T getBean(Class<T> beanType) {
    // Key(클래스 타입)를 이용해 Value(인스턴스)를 조회
    Object bean = beans.get(beanType);

    // 찾는 Bean이 등록되지 않았을 경우 예외
    if (bean == null) {
      throw new RuntimeException(beanType.getName() + " 타입의 Bean을 찾을 수 없습니다.");
    }

    // 객체를 T로 형변환 한 뒤 반환
    return (T) bean;
  }
}
