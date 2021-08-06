# 자원을 직접 명시하지 말고 의존성 주입을 사용하라

대부분의 클래스는 하나 이상의 리소스에 의존한다. 예를 들어 `Dictionary`를 사용해 스펠링 검사를 하는 `SpellChecker` 클래스를 구현한다고 할 때 다음과 같이 구현하는 경우가 있다.

## 부적절한 구현 예시

### static 유틸 클래스([아이템4](../item4/item4.md))

```java
// 부적절한 static 유틸리티 사용 예 - 유연하지 않고 테스트 할 수 없다.
public class SpellChecker {

    private static final Lexicon dictionary = new KoreanDicationry();

    private SpellChecker() {
        // Noninstantiable
    }

    public static boolean isValid(String word) {
        throw new UnsupportedOperationException();
    }


    public static List<String> suggestions(String typo) {
        throw new UnsupportedOperationException();
    }
}

interface Lexicon {}

class KoreanDicationry implements Lexicon {}
```

### 싱글톤으로 구현([아이템3](../item3/item3.md))

```java
// 부적절한 싱글톤 사용 예 - 유연하지 않고 테스트 할 수 없다.
public class SpellChecker {

    private final Lexicon dictionary = new KoreanDicationry();

    private SpellChecker() {
    }

    public static final SpellChecker INSTANCE = new SpellChecker() {
    };

    public boolean isValid(String word) {
        throw new UnsupportedOperationException();
    }


    public List<String> suggestions(String typo) {
        throw new UnsupportedOperationException();
    }
}
```

위의 두 예시처럼 구현하는 경우 다음과 같은 문제점이 있다.

1. 한국어가 아니라 영어에 대한 스펠체커로 바꿔서 사용하고 싶다.
2. 테스트를 할 때, 위한 테스트용 사전을 따로 사용하고 싶다.

-> `SpellChecker`의 구현을 변경해야함

어떤 클래스가 사용하는 리소스에 따라 다른 행동을 하도록 구현하고 싶다면 스태틱 유틸 클래스나 싱글톤을 사용하지 말고 의존성 주입을 이용하자.

의존성 주입이란 자신이 사용할 리소스를 외부로부터 주입 받는 패턴을 말한다.

## 적절한 구현 예시

```java
public class SpellChecker {

    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public static boolean isValid(String word) {
        throw new UnsupportedOperationException();
    }

    public static List<String> suggestion(String typo) {
        throw new UnsupportedOperationException();
    }
}
```

이 패턴의 쓸만한 변형으로 생성자에 팩토리를 넘겨주는 방식이 있다. `Supplier<T>` 인터페이스는 그런 팩토리를 표현하는 완벽한 예이다.

```java
Mosaic create(Supplier<? extends Tile> tileFactory) {...}
```

의존성 주입은 좋은 패턴이지만 의존성이 수천 개 되는 큰 프로젝트에서는 코드를 어지럽게 만들 수 있다. 이럴 때는 Dagger, Juice, Spring 같은 DI 프레임워크를 사용하면 좋다.
