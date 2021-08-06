# 인스턴스화를 막으려면 private 생성자를 사용하라.

`java.lang.Math`, `java.util.Arrays`, `java.util.Collections`처럼 static 메서드 혹은 팩토리 메서드만 모아놓은 유틸리티 클래스는 인스턴스로 만들어 쓰려고 설계한 클래스가 아니다.

그렇다고 생성자를 비워두면 컴파일러가 자동으로 기본 생성자를 추가하기 때문에 클라이언트 입장에서 설계 의도를 제대로 알기 어렵다.

인스턴스화를 막기 위해서 추상클래스로 만드는 것은 제대로된 해결이 아니다. 상속을 받으면 인스턴스를 만들 수 있으니까

인스턴스화를 막기 위해서는 private 생성자를 만들자

```java
public class UtilityClass {
    // 인스턴스화 방지용
    private UtilityClass() {
        throw new AssertionError();
    }
}
```

`AssertionsError`를 추가하는게 필수는 아니다. 다만 실수로라도 클래스 내에서 생성자를 호출하는 것을 방지한다. 하위 클래스에서 생성자를 호출할 수 없기 때문에 상속도 불가능하다. 

생성자는 있는데 호출을 할 수 없다는게 약간 직관적이지 못하다. 그러니 되도록 예시처럼 주석을 달아주도록한다.

추가적으로 `org.springframework.util` 패키지의 `StringUtils` 클래스를 보면 추상 클래스로 선언이 되어있다.

![E7EA689F-0E95-41A1-BD2D-45774C67F996](https://user-images.githubusercontent.com/63030569/128452325-75188ba4-354f-47ac-bb64-1de0eb364c9d.png)

다른 유틸 클래스를 확인해보면 모두 `abstract`로 선언되어있다. 스프링에서는 그냥 추상클래스를 이용해서 인스턴스 불가화를 하는게 규칙인것 같다.  

