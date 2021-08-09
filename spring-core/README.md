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
    
스프링 컨테이너를 생성할 때는 구성 정보를 지정해줘야하는데, XML 방식으로 설정할 수도 있고 자바 코드 기반 애너테이션으로 설정할 수도 있다.

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

`BeanFactory`는 스프링 컨테이너의 최상위 인터페이스이다. 스프링 빈을 관리하고 조회하는 기능을 한다.

`ApplicationContext`는 BeanFactory를 상속하는 하위 인터페이스인데, 애플리케이션에 필요한 여러 부가 기능을 추가로 제공하므로 거의 대부분의 경우에는 ApplicationContext를 사용한다.

`ApplicationContext`가 추가적으로 상속하는 인터페이스에는 다음과 같은 것들이 있다.
* `MessageSource`: 메시지 관련 기능(국제화 등)
* `EnvironmentCapable`: 환경 변수 관련 기능(로컬, 개발, 배포 등의 환경 따로 관리하는 등)
* `ApplicationEventPublisher`: 이벤트 발행-구독 모델 편의 기능 지원
* `ResourceLoader`: 편리한 리소스 조회 기능, 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회

### 스프링 빈 설정 메타 정보(`BeanDefinition`)

XML, 자바, 코틀린 등 다양한 설정 정보 형식을 지원할 수 있는 이유는 빈 설정 정보가 `BeanDefinition`으로 추상화되어있기 때문이다. 빈 설정 메타정보라고 부른다.

`Bean` 하나 당 하나의 메타정보가 생성된다. 스프링 컨테이너는 오직 `BeanDefinition`에 의존해서 설정정보를 읽어온다. 

[컨테이너와 설정정보 의존관계 그림]

ac.getBeanDefinition(beanName)으로 해당 빈의 `BeanDefinition`을 가져올 수 있다. BeanDefinition에는 다음과 같은 정보들이 있다.

* BeanClassName: 생성할 빈의 클래스 명(자바 설정 처럼 팩토리 역할의 빈을 사용하면 없음) 
* factoryBeanName: 팩토리 역할의 빈을 사용할 경우 이름, 예) appConfig 
* factoryMethodName: 빈을 생성할 팩토리 메서드 지정, 예) memberService
* Scope: 싱글톤(기본값)
* lazyInit: 스프링 컨테이너를 생성할 때 빈을 생성하는 것이 아니라, 실제 빈을 사용할 때 까지 최대한 생성을 지연처리 하는지 여부
* InitMethodName: 빈을 생성하고, 의존관계를 적용한 뒤에 호출되는 초기화 메서드 명 DestroyMethodName: 빈의 생명주기가 끝나서 제거하기 직전에 호출되는 메서드 명 
* Constructor arguments, Properties: 의존관계 주입에서 사용한다. (자바 설정 처럼 팩토리 역할 의 빈을 사용하면 없음)

> 실무에서 BeanDefinition을 직접 정의해서 쓰는 일은 거의 없다고 한다.


## 싱글톤 레지스트리

스프링은 기업용 온라인 서비스를 지원하기 위해서 탄생했다. 웹 서비스는 보통 여러 고객이 동시에 요청을 보낸다. 각 고객의 요청마다 새로 객체를 생성하면 응답성이 떨어질 것이다. 그래서 스프링은 기본적으로 빈을 싱글톤으로 관리한다.

일반적으로 개발자가 직접 싱글톤 패턴을 구현하려면 유연성이 떨어지고 테스트하기 어려운 등 여러 문제가 있다. 하지만 스프링 컨테이너에 등록된 빈을 사용하면 직접 싱글톤으로 구현하지 않아도 스프링이 알아서 싱글톤으로 관리해준다. 따라서 아래 테스트는 통과한다.

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
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(discountPolicy(), memberRepository());
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

스프링은 CGLIB이라는 바이트코드 조작 라이브러리를 이용해서 `AppConfig`라는 클래스를 상속받은 임의의 클래스를 만들고 이를 빈으로 등록한다.
이 임의의 클래스는 바이트 코드 조작을 통해, 예컨대 아래처럼 싱글톤 유지를 위한 코드를 추가한다.

[CGLIB 그림]

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

