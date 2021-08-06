# 싱글턴은 private 생성자나 Enum 타입으로 강제하라

[싱글턴 패턴](https://en.wikipedia.org/wiki/Singleton_pattern)은 어플리케이션 내에 그 클래스의 객체가 단 하나만 만들어지도록 강제하는 방법이다.

이 장에서는 싱글턴은 만드는 방법 세 가지를 소개한다.

## final 필드

생성자를 private으로 막아두고 final 필드로 인스턴스를 제공한다.

```java
public class Singleton1 {

    public static final Singleton1 INSTANCE = new Singleton1();
    
    private Singleton1() {}
}
```

클라이언트에서는 아래처럼 인스턴스를 얻는데, private 생성자는 Singleton1.INSTANCE를 초기화할 때 딱 한 번만 호출되므로 싱글턴이 보장된다.
단 리플렉션을 사용해 private 생성자를 호출하면 객체를 두 개 이상 만들 수 있는데 이에 대한 예외처리를 따로 해줘야한다(flag 변수를 둔다던지).

```java
public class SingletonTest {

    public static void main(String[] args) {
        Singleton1 singleton1 = Singleton1.INSTANCE;
    }
}
```

이런 방식의 **장점**은 두 번째 방법인 static 팩토리 메서드를 사용하는 방법에 비해 명확하고 간결하다는 것이다.

## static 팩토리 메서드

두 번째 방법은 생성자와 인스턴스 필드는 private으로 접근을 차단하고 팩토리 메서드를 하나 두는 것이다.

```java
public class Singleton2 {

    private static final Singleton2 INSTANCE = new Singleton2();
    
    private Singleton2() {};

    public static Singleton2 getInstance() {
        return INSTANCE;
    }
}
```

이 방식도 리플렉션을 통한 예외는 똑같이 적용된다.

이러한 방식의 **장점**은 **1.API를 바꾸지 않고도 싱글턴이 아니게 만들 수 있다**는 것이다. 무슨 소리냐면

```java
public class Singleton2 {

    private static final Singleton2 INSTANCE = new Singleton2();
    
    private Singleton2() {};

    public static Singleton2 getInstance() {
        return new Singleton2();
    }
}
```

위처럼 바꿔도 클라이언트에서는 코드를 변경할 일이 없다는 것이다.

```java
public class SingletonTest {

    public static void main(String[] args) {
        Singleton2 singleton2 = Singleton2.getInstance();
    }
}
```

예컨대 애플리케이션 전체에서 유일한 인스턴스를 반환하는 방법에서 코드를 호출하는 쓰레드별로 새로운 인스턴스를 반환하는 방식으로 바꾸고 싶을 때 두 번째 방식을 고려할 수 있다(첫번째 방식을 사용하면 API를 바꾸지 않고는 인스턴스 생성 방식을 변경할 수 없다.).

또 **2. 제네릭 싱글턴 팩토리로 만들 수도 있다.(아이템 30)**

**3. 정적 팩토리의 메서드 참조를 `Supplier`로 사용할 수 있다.**

```java
Supplier<Singleton2> instance = Singleton2::getInstance;
```

## 직렬화(Serialization)

위에서 살펴본 두 방법 모두, 직렬화에 사용한다면 역직렬화 할 때 같은 타입의 인스턴스가 여러개 생길 수 있다. 그 문제를 해결하려면 모든 인스턴스 필드에 transient를 추가 (직렬화 하지 않겠다는 뜻) 하고 readResolve 메소드를 다음과 같이 구현하면 된다. 

```java
    
    private static final transient Singleton2 INSTANCE = new Singleton2();

    private Object readResolve() {
        return INSTANCE;
    }
    
```

## Enum

첫 번째, 두 번째 방법은 원래 알고 있었는데 이 책에서 **가장 바람직한 방법**으로 제시하는 세 번째 방법은 처음 알았다. 그리고 솔직히 좀 놀랐다.

```java
public enum Singleton3 {
    INSTANCE;
}
```

이 방법의 단점은 enum 말고 다른 상위 클래스를 상속할 수 없다는 것이다. 인터페이스는 구현 가능하다.

## 스프링

이 책에서는 바닐라 자바로 싱글톤을 구현하는 방법을 설명하고 있지만, 사실 그냥 스프링을 쓰면 된다.

빈으로 등록된 클래스는 해당 컨텍스트 내에서 싱글톤이 보장된다.


참고

[자바 직렬화의 비밀](https://www.oracle.com/technical-resources/articles/java/serializationapi.html)
[백기선님 유튜브](https://www.youtube.com/watch?v=xBVPChbtUhM&list=PLfI752FpVCS8e5ACdi5dpwLdlVkn0QgJJ&index=3)
