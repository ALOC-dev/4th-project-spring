# spring🌱

# 🌱 이듬해 질 녘 꽃 피는 봄

> Spring Framework의 핵심 원리(IoC/DI, MVC)를 이해하기 위해 최소 기능(MVP)을 직접 구현해보는 리버스 엔지니어링 프로젝트

<br>

## 📖 프로젝트 소개

단순히 프레임워크를 사용하는 개발자를 넘어, "이 기술이 왜 필요하고, 어떤 원리로 동작하는지를 자신 있게 설명할 수 있는 개발자"가 되는 것을 목표로 합니다. 사용자의 관점에서
벗어나 설계자의 관점을 체득하기 위해, 가장 널리 쓰이는 Spring Framework의 심장부인 IoC 컨테이너와 MVC 엔진을 밑바닥부터 만들어봅니다.

<br>

## ✨ 주요 기능

### 1. IoC/DI 컨테이너

- **`ComponentScanner`**: Bean으로 등록할 모든 클래스를 자동으로 찾는 스캐너
- **`BeanFactory`**: Bean을 등록하여 저장하고 의존성 주입을 담당하는 클래스
- **`@Component`**: Bean으로 등록할 클래스를 지정하는 기능
- **`@Autowired`**: 의존 관계에 있는 Bean을 자동으로 주입하는 기능 (생성자 주입)

### 2. MVC 프레임워크

- **`DispatcherServlet`**: 모든 웹 요청을 단일 입구로 받아 다른 컨트롤러/핸들러로 넘겨주는 프론트 컨트롤러
- **`HandlerMapping`**, **`HandlerMethod`**: 각 URL 요청과 해당 요청을 실행할 컨트롤러의 메소드를 매핑하여 저장하는 기능
- **`HandlerAdapter`**: 요청을 처리할 컨트롤러의 메소드를 호출하고 실행 결과를 문자열 응답으로 변환하는 기능
- **`@Controller`**, **`@RestController`**: 받은 URL 요청을 실제로 처리하는 개별 컨트롤러
- **`@GetMapping`**: GET 요청을 처리하는 메서드를 지정하는 기능

### 3. AOP (Aspect-Oriented Programming)

- **`@Aspect`**, **`@Around`**: 프록시를 통해 특정 메서드의 실행 전후에 공통 로직을 삽입하는 기능

<br>

## 🛠️ 기술 스택

- **Language**: Java 17
- **Build Tool**: Gradle
- **Test Framework**: JUnit 5 (Jupiter)
- **Core Technologies**: Java Reflection API, Custom Annotations, Servlet API
- **Web Application Server**: Jetty

<br>

## ⚙️ 실행 방법

- Jetty 웹 서버 기반으로 실행하며, localhost:8080 포트를 통해 웹 페이지에 접속하여 URL 요청을 보낼 수 있다.
- 시작할 때 터미널에 다음과 같이 입력한다.
  > `./gradlew :app:jettyRun`

---

#### 현재 접속 가능한 URL

- **/health** : 헬스체크 기능. 별도의 컨트롤러 매핑 없이 `DispatcherServlet` 내에서 직접 처리하며, 현재 서블릿과 빈팩토리의 상태, 인식된 핸들러의
  개수, 빈의 정보 등을 화면에 출력한다.


- **/hello** : `HelloController`에서 처리하는 테스트용 URL 요청. 성공 여부와 간단한 텍스트를 화면에 출력한다.

<br>

## 👨‍💻 팀원

|    역할    | 이름  |       GitHub        |
|:--------:|:---:|:-------------------:|
|  👑 리더   | 이태권 | github.com/jigun058 |
| 🧑‍💻 팀원 | 나윤서 | github.com/seonooy  |
| 🧑‍💻 팀원 | 이채우 |  github.com/2fill   |
| 🧑‍💻 팀원 | 황지인 |  github.com/sjxp05  |

<br>

## 🗓️ 개발 계획

- **1-4주차**: IoC/DI 컨테이너 구현 (Annotation, Component Scan, Bean Factory, Dependency Injection)
- **5-10주차**: MVC 프레임워크 구현 (DispatcherServlet, Handler Mapping/Adapter)
- **11-12주차**: AOP 구현 (Dynamic Proxy, Advice)
- **13-14주차**: 최종 리팩토링, 문서화 및 발표 준비