# [Spring Core](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8)

# 객체지향원칙(SOLID)

`OrderServiceImpl`은 `DiscountPolicy`와 `MemberRepository`에 의존하고 있다.

```java
public class OrderServiceImpl implements OrderService{

    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    
    ...
}
```

하지만 실제 코드를 보면 인터페이스인 `DiscountPolicy`와 구체 클래스인 `FixDiscountPolicy` 모두에 의존하고 있다. `MemberRepository`도 마찬가지다(DIP 위반).

따라서 할인 정책을 변경한다면 이에 의존하고 있는 `OrderServiceImpl`의 코드도 변경해야한다(OCP 위반).

`OrderServiceImpl`이 인터페이스에만 의존하도록 만들고 싶다면 어떻게 해야할까? 구체 클래스를 외부에서 주입해주면 된다.
```java
public class OrderServiceImpl implements OrderService{

    private final DiscountPolicy discountPolicy;
    private final MemberRepository memberRepository;

    public OrderServiceImpl(DiscountPolicy discountPolicy, MemberRepository memberRepository) {
        this.discountPolicy = discountPolicy;
        this.memberRepository = memberRepository;
    }
    ...
}
```

그러려면 구체 클래스를 생성하고 주입해주는 책임을 가진 독립적인 오브젝트를 새로 만들어주어야한다.

> AppConfig.java
```java
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(new FixDiscountPolicy(), new MemoryMemberRepository());
    }
}
```

클라이언트 코드에서는 `AppConfig`를 통해 필요한 오브젝트를 전달받는다.

> OrderServiceTest.java
```java
    AppConfig appConfig = new AppConfig();
    MemberService memberService = appConfig.memberService();
    OrderService orderService = appConfig.orderService();
```

이제 인터페이스의 구현체를 변경하더라도 AppConfig의 코드만 변경하면 그 외 다른 코드는 일절 수정하지 않아도 된다.

# 스프링

## 스프링 적용하기

* 키워드
    * @Configuration
    * @Bean
    * ApplicationContext
    * getBean()

바닐라 자바로 짠 코드를 스프링으로 변경해보자.

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    ...
}
```

`@Configuration` 애너테이션을 붙인 클래스는 스프링 컨테이너(ApplicationContext)를 구성하는 설정정보로 사용된다.
여기서 `@Bean` 애너테이션이 붙은 메서드를 모두 호출하여 반환된 객체를 스프링 컨테이너에 등록한다. 이렇게 스프링 컨테이너에 등록된 객체를 "스프링 빈"이라고 부른다.

스프링 빈은 `@Bean`이 붙은 메서드의 이름을 스프링 빈의 이름을 사용한다.

빈을 사용하려면 `ApplicationContext`에서 `getBean()`메서드를 통해 빈을 검색해서 찾아오면된다.

> OrderServiceTest.java
```java
class OrderServiceTest {

    MemberService memberService;
    OrderService orderService;

    @BeforeEach
    void beforeEach() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        memberService = ac.getBean("memberService", MemberService.class);
        orderService = ac.getBean("orderService", OrderService.class);
    }
    ...
}
```

첫 번째 인자는 빈의 이름, 두 번째는 필요한 타입인데, 이름만으로 빈을 검색하는 것도 가능하지만 `Object` 타입으로 반환되기 때문에 캐스팅해는 번거로움이 있다.
그래서 두 번째 인자로 타입을 넣어주면 캐스팅 과정을 생략할 수 있다.

## 스프링 컨테이너와 스프링 빈

* 키워드
    * 스프링 컨테이너
    * BeanFactory
    * ApplicationContext
    * 빈 출력하기
    * BeanDefinition
    
스프링 컨테이너를 생성할 때는 설정 정보를 지정해줘야하는데, XML 방식으로 설정할 수도 있고 자바 코드 기반 애너테이션으로 설정할 수도 있다.

빈 이름: `name = ` 속성으로 빈 이름을 직접 정해줄 수도 있다. 빈 이름이 중복되면 무시하거나 덮어써버리므로 주의한다.

스프링 컨테이너는 설정 정보를 참조해서 의존관계를 주입한다. 스프링은 빈을 생성하고, 의존관계를 주입하는 단계가 나누어져 있다. 
그런데 예시의 `AppConfig`처럼 자바 코드로 스프링 빈을 등록하면 생성자를 호출하면서 의존관계 주입도 한번에 처리된다.

### 빈 조회하기

`ac.getBeanDefinitionNames()`: 모든 빈 이름을 배열로 반환한다.

`ac.getBean()`: 특정 빈을 검색한다. 이름, 타입, (이름, 타입) 등을 넘겨 조회할 수 있다. 해당 빈이 없다면 `NoSuchBeanDefinitionException`이 발생한다.

`ac.getBeanDefinition(beanDefinitionName)`: 빈 정보 객체(`BeanDefinition`)를 반환한다. getRole() 메서드로 역할을 확인할 수 있다. `ROLE_APPLICATION`은 사용자가 정의한 빈
`ROLE_INFRASTRUCTURE`은 스프링 내부에서 사용하는 빈이다.

> ❗️ 타입으로 빈을 조회할 때 같은 타입의 빈이 여러 개 등록되어 있다면 예외가 발생한다. 이때는 이름을 같이 넘겨줘야한다. 
> 
> `ac.getBeansOfTypes(RequiredType.class)`를 호출하면 해당 타입의 빈을 모두 조회해 Map<String, Type> 형식으로 반환한다.

> ❗부모 타입으로 빈 조회시 자식 타입까지 모두 가져온다.

### BeanFactory와 ApplicationContext

**`BeanFactory`는 스프링 컨테이너의 최상위 인터페이스이다.** 스프링 빈을 관리하고 조회하는 기능을 한다.

`ApplicationContext`는 BeanFactory를 상속하는 하위 인터페이스인데, **BeanFactory 이외에도 여러 인터페이스를 추가적으로 상속**하여 애플리케이션에 필요한 여러 부가 기능을 추가로 제공한다. 거의 대부분의 경우에는 ApplicationContext를 사용한다.

`ApplicationContext`가 추가적으로 상속하는 인터페이스에는 다음과 같은 것들이 있다.
* `MessageSource`: 메시지 관련 기능(국제화 등)
* `EnvironmentCapable`: 환경 변수 관련 기능(로컬, 개발, 배포 등의 환경 따로 관리하는 등)
* `ApplicationEventPublisher`: 이벤트 발행-구독 모델 편의 기능 지원
* `ResourceLoader`: 편리한 리소스 조회 기능, 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회

### 스프링 빈 설정 메타 정보(`BeanDefinition`)

XML, 자바, 코틀린 등 다양한 설정 정보 형식을 지원할 수 있는 이유는 빈 설정 정보가 `BeanDefinition`으로 추상화되어있기 때문이다. 빈 설정 메타정보라고 부른다.

`Bean` 하나 당 하나의 메타정보가 생성된다. 스프링 컨테이너는 오직 `BeanDefinition`에 의존해서 설정정보를 읽어온다. 

![C3A581F2-9308-4D64-9229-6E9525D24395](https://user-images.githubusercontent.com/63030569/130098310-269b7934-1fef-45b6-ba4d-d7017cb5672a.png)

ac.getBeanDefinition(beanName)으로 해당 빈의 `BeanDefinition`을 가져올 수 있다. BeanDefinition에는 다음과 같은 정보들이 있다.

* BeanClassName: 생성할 빈의 클래스 명(자바 설정 처럼 팩토리 역할의 빈을 사용하면 없음) 
* factoryBeanName: 팩토리 역할의 빈을 사용할 경우 이름, 예) appConfig 
* factoryMethodName: 빈을 생성할 팩토리 메서드 지정, 예) memberService
* Scope: 싱글톤(기본값)
* lazyInit: 스프링 컨테이너를 생성할 때 빈을 생성하는 것이 아니라, 실제 빈을 사용할 때 까지 최대한 생성을 지연처리 하는지 여부
* InitMethodName: 빈을 생성하고, 의존관계를 적용한 뒤에 호출되는 초기화 메서드 명 DestroyMethodName: 빈의 생명주기가 끝나서 제거하기 직전에 호출되는 메서드 명 
* Constructor arguments, Properties: 의존관계 주입에서 사용한다. (자바 설정 처럼 팩토리 역할 의 빈을 사용하면 없음)

xxxAplicationContext는 `xxxBeanDefinitionReader`를 통해 설정정보(이를테면 AppConfig.java)를 읽어서 BeanDefinition을 생성한다. 따라서 새로운 형식의 설정 정보가 추가되면
그 형식에 맞는 xxxBeanDefinitionReader를 만들어 BeanDefinition을 생성하면 된다.

> 실무에서 BeanDefinition을 직접 정의해서 쓰는 일은 거의 없다고 한다.


## 싱글톤 레지스트리

* 키워드
  * 싱글톤
  * @Configuration
  * CGLIB

스프링은 기업용 온라인 서비스를 지원하기 위해서 탄생했다. 웹 서비스는 보통 여러 고객이 동시에 요청을 보낸다. 각 고객의 요청마다 새로 객체를 생성하면 응답성이 떨어질 것이다. 그래서 스프링은 기본적으로 빈을 싱글톤으로 관리한다.

일반적으로 개발자가 직접 싱글톤 패턴을 구현하려면 유연성이 떨어지고 테스트하기 어려운 등 여러 문제가 있다. 하지만 스프링 컨테이너에 등록된 빈을 사용하면 직접 싱글톤으로 구현하지 않아도 스프링이 알아서 싱글톤으로 관리해준다. 따라서 아래 테스트는 통과한다.

> 싱글톤 패턴의 문제점
> * 싱글톤 패턴을 구현하는 코드 자체가 많이 들어간다.
> * 의존관계상 클라이언트가 구체 클래스에 의존한다. DIP를 위반한다.
> * 클라이언트가 구체 클래스에 의존해서 OCP 원칙을 위반할 가능성이 높다. 
> * 테스트하기 어렵다.
> * 내부 속성을 변경하거나 초기화 하기 어렵다.
> * private 생성자로 자식 클래스를 만들기 어렵다.
> * 결론적으로 유연성이 떨어진다.
> * 안티패턴으로 불리기도 한다

```java
public class SingletonTest {

    @Test
    void singletonTest() throws Exception {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService1 = ac.getBean("memberService", MemberService.class);
        MemberService memberService2 = ac.getBean("memberService", MemberService.class);

        assertThat(memberService1).isSameAs(memberService2);
    }
}
```

> 주의❗ 빈의 필드에 공유 변수를 두면 안된다.
> 빈은 싱글톤으로 관리되기 때문에 상태를 유지하는 필드를 두면 race condition으로 인한 버그가 발생할 수 있다. 그렇기 때문에 빈은 반드시 무상태로 설계해야한다.

### @Configuration과 CGLIB

`AppConfig`의 코드는 다음과 같다.

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());  // 여기서 한 번 호출
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(discountPolicy(), memberRepository());  // 여기서 또 호출
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```
️
memberService()와 orderService()를 보면 `MemberRepository`의 생성자가 두 번 호출되고 있는 것을 볼 수 있다. 제아무리 스프링이라도 쌩 자바 코드로 생성자를 직접 두 번 호출했는데 어떻게 싱글톤으로 관리를 할 수 있는걸까?

memberRepository() 메서드가 호출될 때 메시지를 출력하도록 만들고 빈을 호출해보자.

```java
@Configuration
public class AppConfig { 
    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call memberRepository");
        return new MemoryMemberRepository();
    }
}
```

```java
    @Test
    void callMemberRepository() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService = ac.getBean("memberService", MemberService.class);
        OrderService orderService = ac.getBean("orderService", OrderService.class);
    }
```

테스트를 실행해보면 콘솔에 "call memberRepository"가 한 번만 출력되는 것을 확인할 수 있다. 

어떻게 이런 일이 가능한 것일까? 비밀은 `@Configuration` 애너테이션에 있다.

`AppConfig`도 빈이므로 컨테이너에서 꺼내올 수 있다. AppConfig의 클래스 정보를 출력해보면 기대했던 값과 다른 결과가 나온다.

```java
    @Test
    void testCGLIB() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        AppConfig bean = ac.getBean(AppConfig.class);
        System.out.println("AppConfig = " + bean.getClass());
    }
```

출력결과
```
AppConfig = class com.study.springcore.AppConfig$$EnhancerBySpringCGLIB$$c299be5b
```

**스프링은 CGLIB이라는 바이트코드 조작 라이브러리를 이용해서 `AppConfig`라는 클래스를 상속받은 임의의 클래스를 만들고 이를 빈으로 등록한다.**
이 임의의 클래스는 바이트 코드 조작을 통해, 예컨대 아래처럼 싱글톤 유지를 위한 코드를 추가한다.


![3FBEF939-7054-4E55-B2AB-9B449DE3B780](https://user-images.githubusercontent.com/63030569/130102816-4d5c538e-2871-43f1-adbf-10f40a420086.png)

```java
    @Bean
    public MemberRepository memberRepository() {
    if (memoryMemberRepository가 이미 스프링 컨테이너에 등록되어 있다면) {
        return 스프링 컨테이너에서 찾아서 반환;
    } else {
        MemoryMemberRepository를 생성하고 스프링 컨테이너에 등록 
        return 반환
    } 
}
```

만약 `AppConfig` 클래스에서 `@Configuration` 애너테이션을 제거하면 AppConfig가 그대로 컨테이너에 등록되고 싱글톤도 보장되지 않는다.

## 컴포넌트 스캔

* 키워드
  * @ComponentScan
  * @Component
  * @Autowired
  * 컴포넌트 중복 등록, 충돌

**스프링은 설정정보가 없어도 자동으로 빈을 등록하는 기능을 제공한다. 이를 컴포넌트 스캔이라고 한다.**

`AppConfig`에 정의한 설정정보를 모두 지우고 `@ComponentScan` 애너테이션을 붙여준다.

```java
@Configuration
@ComponentScan
public class AppConfig {}
```

컴포넌트 스캔은 이름 그대로 `@Component` 애너테이션이 붙은 클래스를 스캔해서 스프링 빈으로 등록한다. 빈으로 등록할 클래스에 `@Component` 애너테이션을 붙이기만 하면된다.

> `@Configuration` 애너테이션에는 `@Component` 애너테이션이 포함되어있기 때문에 자동으로 빈으로 등록된다.

의존관계 주입을 자동으로 하기 위해서는 `@Autowired` 애너테이션을 붙여줘야한다. 의존관계 주입을 설정하는 방법에는 여러가지가 있는데, 일반적으로 생성자 주입이 권장된다. 이유는 뒤에서 정리

```java
@Component
public class OrderServiceImpl implements OrderService { 
    private final DiscountPolicy discountPolicy;
    private final MemberRepository memberRepository;

    @Autowired
    public OrderServiceImpl(DiscountPolicy discountPolicy,
          MemberRepository memberRepository) {
        this.discountPolicy = discountPolicy;
        this.memberRepository = memberRepository;
    }
    
    ...
}
```

이렇게 하면 스프링이 자동으로 컨테이너에 등록된 빈을 조회(기본은 타입 조회)해서 의존성을 주입해준다.

### 탐색 위치

속성을 설정해 탐색 범위를 정할 수 있다.

```java
@ComponentScan(
    basePackages = "com.study"
)
```

위처럼 시작 위치를 정하면 해당 패키지를 포함한 하위 패키지를 탐색의 대상으로 설정한다. {"A", "B"} 형태로 여러 패키지를 지정할 수도 있다.

스프링부트의 경우 설정정보 클래스를 프로젝트 최상단 패키지에 두고 basePackages 설정을 생략하는 방식을 사용한다. 스프링부트로 프로젝트를 생성하면 XXXApplication 클래스가 프로젝트 패키지에 자동으로 생성되는데 여기 붙은 `@SpringBootApplication` 애너테이션에 `@ComponentScan` 애너테이션이 포함되어있다. 이때 탐색 범위는 설정정보 클래스가 위치한 패키지를 포함한 그 하위 패키지이다.

![image](https://user-images.githubusercontent.com/63030569/130560170-7c17c85c-a7f4-4aa5-869d-1e7a850685c1.png)

`includeFilters`, `excludeFilters` 속성을 추가하여, 컴포넌트 스캔에 포함하거나 제외할 대상을 선택할 수 있다.

```java
@ComponentScan(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyExcludeComponent.class)
  )
```


### 기본 대상

`@Component` 뿐만 아니라 아래의 애너테이션들도 스캔의 대상이 된다. 스테레오타입 애너테이션이라고 부른다.
* @Component: 컴포넌트 스캔에서 사용
* @Controller: 스프링 MVC 컨트롤러에서 사용
* @Service: 스프링 비즈니스 로직에서 사용, 특별한 처리를 하지 않는다. 대신 개발자들이 핵심 비즈니스 로직이 여기에 있겠구나 라고 비즈니스 계층을 인식하는데 도움이 된다.
* @Repository: 스프링 데이터 접근 계층에서 사용, 데이터 계층의 예외를 스프링 예외로 변환해준다.
* @Configuration: 설정정보에서 사용,  스프링 빈이 싱글톤을 유지하도록 추가 처리를 한다.



### 중복 등록과 충돌

같은 빈 이름이 등록될 때 충돌이 날 수 있는 경우는 다음 두 가지가 있다.

1. 자동 빈 등록 vs 자동 빈 등록

같은 이름의 빈이 중복해서 자동 등록 대상이 될 때는 `ConflictingBeanDefinitionException` 예외가 발생한다.

2. 자동 빈 등록 vs 수동 빈 등록

이 경우에는 수동 빈 등록이 우선권을 가진다. 그렇지만 이런 경우에 버그가 발생하면 찾기가 매우 어렵기 때문에 최근 스프링 부트는 이 경우에 오류가 발생하도록 기본 설정 값을 바꾸었다.


## 의존관계 자동 주입(feat. 뭐가 제일 좋을까?)

* 키워드
  * 의존관계 주입 방법
  * 의존관계 주입과 의존관계 검색
  * @Qualifier, @Primary
  * Dependency Injection vs Dependency Lookup
  
의존관계 자동 주입에는 크게 네 가지 방법이 있다.

* 생성자 주입
* 수정자 주입(setter 주입)
* 필드 주입
* 일반 메서드 주입

### 생성자 주입

특징

* 생성자 호출 시점에 딱 한 번만 호출되는 것이 보장된다.
* 불변, 필수 의존관계에서 사용한다.
* 주로 선호되는 방법이다. [스프링 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-setter-injection)에서도 생성자 주입의 사용을 권장하고 있다.

```java
    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
```

**❗️생성자가 하나만 있는 경우 `@Autowired` 애너테이션을 생략해도 생성자 주입이 일어난다.** 이 특징을 이용해서 다음과 같이 롬복과 final 키워드를 통해 자동주입을 할 수 있다.

```java
@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;
}
```

### 수정자 주입

자바빈 프로퍼티의 세터에 @Autowired를 붙여 의존관계를 주입하는 방법이다. **선택, 변경** 가능성이 있는 의존관계에 사용한다.

```java
    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
```

이런 경우 가끔 주입할 대상이 없어도 동작해할 때가 있는데, @Autowired의 기본 동작은 주입할 대상이 없으면 오류를 발생시킨다. 이때는 required = false 속성을 주면 해결된다.

### 필드 주입

필드 위에 @Autowired 애너테이션을 붙여서 주입하는 방법이다. 결론부터 말하자면 사용하지 말자. 간편하긴 하지만 외부에서 변경이 불가해 테스트하기가 힘들며 불변으로 선언하는 것도 불가능하다.
**애플리케이션의 실제 코드와 관계 없는 테스트 코드**나 **스프링 설정을 목적으로 하는 @Configuration 같은 곳에서만 특별한 용도로 사용**하자.


### 일반 메서드 주입

일반 메서드를 통해서도 의존 관계를 주입받을 수 있다. 한 번에 여러 파라미터를 받을 수 있다는 장점이 있지만 자주 사용되지는 않는다.
```java
    @Autowired
        public void init(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
            this.memberRepository = memberRepository;
            this.discountPolicy = discountPolicy;
        }
```

### 자동 주입 대상을 옵션으로 처리하는 방법

* required = false 속성 주기
* 파라미터에 @Nullable 애너테이션 붙이기: 자동 주입 대상이 없으면 null 주입
* 파라미터를 Optional<>로 감싸기: 자동 주입 대상이 없으면 Optional.empty 주입


### 조회 대상 빈이 두 개 이상일 때는?

`@Autowired`는 기본적으로 타입으로 대상 빈을 조회한다. 그런데 만약 같은 타입을 상속 혹은 구현하는 빈이 여러 개 있다면 어떤 방법으로 원하는 빈을 주입할 수 있을까?

우선 아무런 처리를 하지 않으면 `NoUniqueBeanDefinitionException`이 발생한다. 당연히 우리가 어떤 구체 타입의 빈을 주입받길 원하는지 명시하지 않았으니 스프링 입장에서는 빈을 주입해줄 수가 없다.

```java
    @Autowired
    DiscountPolicy discountPolicy;   // FixDiscountPolicy와 RateDiscountPolicy 중 어떤 것을 주입 해야할지 모르므로 오류 발생 
```

그렇다고 하위 타입을 지정해버리면 DIP를 위배하고 유연성도 떨어진다.


1. 필드 명으로 매칭

똑똑하게도 스프링은 타입으로 매칭해본 뒤 여러 개가 있으면 필드명(혹은 파라미터명)으로 빈 이름을 추가 매칭한다.

```java
    @Autowired
    DiscountPolicy fixDiscountPolicy;  // FixDiscountPolicy 주입
```

2. @Qualifier

@Qualifier는 추가적인 구분자를 붙여주는 것이다. 물론 추가로 매칭할 정보를 주는 것이지 빈 이름을 변경하는 것은 아니다.

아래와 같이 빈 등록시 @Qualifier와 이름을 주고, 주입 받을 때 @Qualifier를 붙이고 등록한 이름을 넣으면 된다. 
```java
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {}
```

```java
    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, 
                @Qualifier("mainDiscountPolicy") DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
}

```

만약 해당 이름으로 등록된 @Qualifier가 없다면 추가적으로 해당 이름의 빈을 찾는다.

3. @Primary

@Primary는 우선권을 주는 기능이다. @Primary가 붙은 빈이 우선적으로 주입된다.


#### 무엇을 사용해야 할까?

@Primary가 @Qualifier에 비해 훨씬 깔끔하다. 따로 이름을 줄 필요도 없고 사용하는 곳에서 번거롭게 애노테이션을 붙여야할 필요도 없다.

만약 같은 타입을 구현한 서브 타입 두 개가 있고 하나는 매우 빈번하게, 하나는 가끔씩 쓰인다면 매우 빈번하게 쓰이는 빈을 @Primary로 지정하고 가끔씩 쓰이는 빈을 @Qualifier로 지정해서
해당 빈이 필요할 때만 @Qualifier 애너테이션을 붙여서 주입하는 것이 깔끔하고 좋다.

> 우선순위
> 스프링은 일반적으로 자동보다는 수동, 넒은 범위의 선택권 보다는 좁은 범위의 선택권이 우선 순위가 높다. @Primary에 비해 @Qualifer가 더 상세하게 정의되기 때문에 @Qualifier가 우선권이 더 높다.
> 따라서 @Primary가 등록되어 있어도 파라미터에 @Qualifer가 있다면 @Qualifer가 매칭된다.

> 🎁 같은 타입의 빈을 구현하는 모든 하위 타입 빈이 필요할 때
> 
> 전략 패턴을 구현하는 등, 원하는 타입의 모든 하위 타입 빈이 필요할 때는 Map이나 List로 해당 타입을 주입받으면 컬렉션에 모든 하위 타입 빈이 주입된다.

> 수동 등록 빈은 언제 사용하는 것이 좋을까?
> 
> AOP, 애플리케이션 전체에 광범위하게 사용되는 기술 지원 로직 등 비즈니스 로직과 달리 특정한 상황에서만 쓰이는 빈들은 명확하게 수동으로 등록해주면 좋다. 
> 또 다형성을 적극 활용하는 빈은 따로 패키지로 묶거나 새로운 설정 정보를 만들어서 수동으로 등록해주면 좋다.
> 어쨌거나 **핵심은 명확하고 가독성이 있어야 한다는 것**이다.


### DI(Dependency Injection)과 DL(Dependency Lookup)

스프링이 제공하는 IoC 방법에는 의존관계 주입 외에도 의존관계 검색이라는 방법이 있다. 이 방법은 의존관계를 외부로부터 주입받는게 아니라 스스로 검색한다. 물론
의존 관계를 맺을 오브젝트를 결정하는 일과 이를 생성하는 일은 외부 컨테이너에게 맡긴다(그게 아니라면 IoC가 아닐테니까...).

```java
    public OrderServiceImpl() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        this.memberRepository = ac.getBean(MemberRepository.class);
    }
```

보통의 경우에는 의존관계 주입을 선택하는 것이 낫다. **의존관계 검색을 사용할 경우 코드 안에 스프링 API가 나타나기 때문**이다. DI와 DL의 가장 큰 차이점은 **DL의 경우 검색하는 오브젝트 자신은 빈일 필요가 없다는 것**이다.

애플리케이션 내에서 DL이 반드시 한 번은 일어나게 되어있는데, 대표적인게 스태틱 main 메서드이다. main 메서드에서는 DI를 이용해 오브젝트를 주입받을 방법이 없기 때문이다. 
서버에는 main()같은 기동 메서드는 없지만 사용자의 요청을 받을 때마다 main() 메서드와 비슷한 역할을 하는 서블릿에서 스프링 컨테이너에 담긴 오브젝트를 사용하려면 한 번은 DL을 사용해야한다. 하지만 이런 서블릿은 스프링이 미리 만들어서 제공하기 때문에 직접 구현할 필요는 없다.
또 테스트코드에서도 DL을 사용한다.
