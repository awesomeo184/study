# 생성자에 매개변수가 많다면 빌더를 고려하라

정적 팩토리와 생성자는 선택적 매개변수가 많을 때 적절한 대응을 하기가 어렵다. 생성자의 매개변수가 많을 때는 우선 **점층적 생성자 패턴**을 고려해볼 수 있다.

아래 예시는 매개변수를 전달하지 않으면 기본값으로 0을 전달한다고 가정한다.

```java
public class Foo {
    private int a;
    private int b;
    private int c;
    
    public Foo(int a) {
        this(a, 0, 0)
    }

    public Foo(int a, int b) {
        this(a, b, 0)
    }

    public Foo(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
```

점층적 생성자 패턴의 **단점**은 **1.결국엔 클라이언트가 사용하길 원하지 않는 매개변수까지 포함해야되는 일이 생긴다는 것**이다. 위 예시를 예로 들면
만약 매개변수 c에만 값을 전달하고 싶으면 결국 `new Foo(0, 0, 10)` 이렇게 코드를 작성해야된다는 것이다.

또 **2.같은 타입의 매개변수가 여러 개 있는 경우, 클라이언트에서 순서를 잘못 전달해도 컴파일 타임에 오류를 잡을 수 없다.** 예를 들어 a=1, b=2, c=3 이렇게 값을 전달하려했는데
실수로 `new Foo(2, 1, 3)` 이렇게 전달해도 오류없이 컴파일된다.

다른 대안으로 **자바빈즈 패턴**을 고려해볼 수 있는데, 기본 생성자를 하나 두고, setter 메서드로 값을 설정하는 것이다. 근데 이 방식은 치명적인 단점이 두 가지 있다.

1. 여러 필드의 값을 설정하려면 세터 메서드를 여러번 호출해야되는데, 이 사이에 객체의 일관성이 깨진다.
2. 불변으로 만들 수 없다(필드에 final 키워드를 선언할 수 없기때문).

가독성은 좋지만 안정성이 너무 떨어지는 방법이다.

점층적 생성자 패턴의 안정성과 자바빈즈 패턴의 가독성을 모두 겸비한 방식이 **빌더 패턴**이다.

아래는 빌더 패턴을 활용한 클래스이다. main 메서드에 클라이언트에서 어떻게 생성하는지를 표현해놓았다. 찬찬히 코드를 읽어보면 어떻게 구현되는지 어렵지 않게 파악할 수 있을 것이다.

```java
public class NutritionFactsByBuilder {

    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {

        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수. 0으로 초기화한다.
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            calories = val;
            return this;
        }

        public Builder fat(int val) {
            fat = val;
            return this;
        }

        public Builder sodium(int val) {
            sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }

        public NutritionFactsByBuilder build() {
            return new NutritionFactsByBuilder(this);
        }
    }

    private NutritionFactsByBuilder(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }

    public static void main(String[] args) {
        NutritionFactsByBuilder cocaCola =
            new NutritionFactsByBuilder.Builder(240, 8)
                .calories(100)
                .sodium(35)
                .carbohydrate(27).build();
    }

}
```

빌더 패턴은 다 좋은데, 클래스를 정의하는 과정에서 보일러 플레이트 코드가 너무 많다. 고맙게도 롬복이 빌더 패턴을 적용해주는 어노테이션을 제공한다.
롬복의 `@Builder` 애너테이션을 사용하면 위 코드를 아래처럼 심플하게 표현할 수 있다.

```java
@Builder(builderMethodName = "requireArgsBuilder")
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static NutritionFactsBuilder builder(int servingSize, int servings) {
        return requireArgsBuilder()
            .servingSize(servingSize)
            .servings(servings);
    }

    public static void main(String[] args) {
        NutritionFacts cocaCola = NutritionFacts.builder(240, 8)
            .calories(100)
            .sodium(35)
            .carbohydrate(27).build();
    }
}
```
