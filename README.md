# spring🌱
# 🌱 이듬해 질 녘 꽃 피는 봄

> Spring Framework의 핵심 원리(IoC/DI, MVC)를 이해하기 위해 최소 기능(MVP)을 직접 구현해보는 리버스 엔지니어링 프로젝트

<br>

## 📖 프로젝트 소개

단순히 프레임워크를 사용하는 개발자를 넘어, "이 기술이 왜 필요하고, 어떤 원리로 동작하는지를 자신 있게 설명할 수 있는 개발자"가 되는 것을 목표로 합니다. 사용자의 관점에서 벗어나 설계자의 관점을 체득하기 위해, 가장 널리 쓰이는 Spring Framework의 심장부인 IoC 컨테이너와 MVC 엔진을 밑바닥부터 만들어봅니다.

<br>

## ✨ 주요 기능

### 1. IoC/DI 컨테이너
- **`@Component`**: 클래스를 스캔하여 Bean으로 등록하는 기능
- **`@Autowired`**: 의존 관계에 있는 Bean을 자동으로 주입하는 기능 (생성자 주입)

### 2. MVC 프레임워크
- **`DispatcherServlet`**: 모든 웹 요청을 단일 입구에서 처리하는 프론트 컨트롤러
- **`@Controller`**, **`@RestController`**: 특정 URL 요청과 처리 메서드를 매핑하는 기능
- **`@GetMapping`**: GET 요청을 처리하는 메서드를 지정하는 기능

### 3. AOP (Aspect-Oriented Programming)
- **`@Aspect`**, **`@Around`**: 프록시를 통해 특정 메서드의 실행 전후에 공통 로직을 삽입하는 기능

<br>

## 🛠️ 기술 스택

- **Language**: Java 17
- **Build Tool**: Gradle
- **Test Framework**: JUnit 5 (Jupiter)
- **Core Technologies**: Java Reflection API, Custom Annotations, Servlet API

<br>

## ⚙️ 실행 방법

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