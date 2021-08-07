# [Spring Core](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8)

## 객체지향원칙(SOLID)

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

