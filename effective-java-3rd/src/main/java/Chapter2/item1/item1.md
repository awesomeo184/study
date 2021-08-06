# 아이템 1. 생성자 대신 정적 팩토리 메서드를 고려하라

- [x] 디자인 패턴에서 팩토리 메서드와 다른 의미이다.

```java
public static Boolean valueOf(boolean b) {
    return b ? Boolean.True : Boolean.False;
    }
```

생성자 대신 그 클래스의 인스턴스를 반환하는 정적 팩토리 메서드(static factory method)를 제공할 수 있다.
생성자를 사용하는 것과 비교할 때, 각각 장단점이 있다. 먼저 장점들은 다음과 같다.

## 장점 1. 이름을 가질 수 있다.

생성자는 항상 클래스와 이름이 같아야한다. 때로는 생성자에 넘기는 매개변수만으로는 반환되는 객체의 특성을 제대로 나타내지 못한다.
반면 정적 팩토리 메서드는 반환되는 객체의 특성을 이름을 통해 드러낼 수 있다. 예를 들어 
`BigInteger(int bitLength, int certainty, Random rnd)`보다 `BigInteger.probablePrime(int bitLength, Random rnd)`이
소수인 BigInteger 객체를 반환한다는 의미가 더 잘 드러난다.

또 같은 시그니처를 가지는 생성자 여러 개가 필요할 때 정적 팩토리 메서드를 이용할 수 있다. 예를 들어 아래와 같이 매개변수의 이름만 달라서는 두 개의 생성자를 만들 수 없다.

```java
public class Student  {
    private String id;
    private String name;

    // 컴파일 불가
    public Student(String id) {
        this.id = id;
    }

    public Student(String name) {
        this.name = name;
    }
}
```

이 때는 정적 팩토리 메서드로 바꾸면 된다.

```java
public class Student  {
    private String id;
    private String name;

    public static Student withId(String id) {
        Student student = new Student();
        student.id = id;
        return student;
    }

    public static Student withName(String name) {
        Student student = new Student();
        student.name = name;
        return student;
    }
}
```

## 장점 2. 호출될 때마다 인스턴스를 새로 생성하지 않을 수 있다.

불변 클래스(아이템 17)는 인스턴스를 미리 만들어 놓거나 새로 생성한 인스턴스를 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.
`Boolean.valueOf(boolean)` 메서드는 아얘 객체를 생성하지 않고 상수를 반환한다. 이 방식은 생성 비용이 큰 같은 객체가 자주 요청되는 상황에서
성능을 상당히 끌어올려준다.

```java
public class Student {
    private static final Student GOOD_STUDENT = new Student();

    public static Student callGoodStudent() {
        return GOOD_STUDENT;
    }
}
```

반복되는 요청에 같은 객체를 반환하는 식으로 인스턴스의 라이프 사이클을 통제하는 클래스를 인스턴스 통제(instance-controlled) 클래스라고 한다.
인스턴스를 통제하면 클래스를 싱글톤(아이템 3)으로 만들 수도 있고, 인스턴스화 불가 아이템(아이템 4)로 만들 수 있다. 또 불변 값 클래스(아이템 17)에서 동치인
인스턴스가 단 하나뿐임을 보장할 수 있다. 인스턴스 통제는 플라이웨이트 패턴의 근간이 되며, 열거 타입은 인스턴스가 하나만 만들어짐을 보장한다.

## 장점 3. 반환 타입의 하위 타입의 객체를 반환할 수 있는 능력이 있다.

반환 타입으로는 상위 타입을 드러내고 내부적으로 구체적인 하위 타입을 결정할 수 있다.

```java
public static Super foo() {
    return new Sub();
    }
```

컬렉션 프레임워크가 이 방법을 적극적으로 활용해 45개의 클래스를 non-public으로 만들어서 프로그래머가 이를 다 익혀야하는 부담감을 줄였다.

```java
public class Collections {
    ...

    // List 인터페이스 반환
    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }

    // 구현체는 non-public class
    static class UnmodifiableList<E> extends UnmodifiableCollection<E> implements List<E> {
        ...
    }
}
```

## 장점 4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.

위 unmodifiableList() 메서드를 보면 타입 체크를 통해서 각기 다른 클래스를 반환하고 있다.

EnumSet의 경우, public static 메서드 allOf()와 of()를 제공하는데, 만약 전달된 Enum 클래스가 64개 이하의 원소를 가지고 있으면
RegularEnumSet의 인스턴스를, 65개 이상이면 JumboEnumSet 인스턴스를 반환한다. 클라이언트 쪽에서는 어떤 타입의 인스턴스가 반환되는지 신경쓰지 않고도
인터페이스를 사용할 수 있다는 장점이 있다.

## 장점 5. 정적 팩토리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

JDBC에서 커넥션을 가져오는 코드는 다음과 같다.

```java
Class.forName("oracle.jdbc.driver.OracleDriver"); 
Connection con = DriverManager.getConnection("jdbc:oracle:@localhost:8080", "ddd", "ddd"); 
```
여기서 getConnection()에 의해 반환되는 구현체는 DBMS의 종류에 따라 달라지게 된다. 정적 팩토리 메서드를 이용하면 각 DBMS에 맞는 API를 따로 
구현하지 않고 클래스가 로드되는 시점에서 구현체를 바꿔치기 할 수 있다. 무슨 말이냐하면

```java
public class DriverManager {
    private DriverManager() {}
    
    public static Connection getConnection(String name) {
        Driver driver = new Driver("default driver name");
        
        // 어떤 약속된 텍스트 파일에서 Driver 구현체의 FQCN을 읽어온다.
        // FQCN에 해당하는 인스턴스를 생성한다.
        // diver 변수가 해당 인스턴스를 참조하도록 한다(바꿔치기 한다).
        // ex) driver = new Driver(name);
        
        return driver;
    }
}
```

이런 식으로 어떤 구현체를 생성할 것인지 미리 결정하지 않고, 클래스가 로드되는 시점에 구현체를 생성해서 반환하는 것이다.

## 단점 1. 상속을 하려면 public이나 protected 생성자가 필요하니 정적 팩토리 메서드만 제공하면 하위 클래스를 만들 수 없다.

예를 들어 `java.util.Collections`는 private 생성자만 있기 때문에 상속을 할 수 없다. 이 제약은 상속보다 컴포지션을 사용하도록 유도하고
불변 타입으로 만들려면 이 제약을 지켜야 한다는 점에서 오히려 장점으로 받아들일 수도 있다.

## 단점 2. 정적 팩토리 메서드는 프로그래머가 찾기 어렵다.
javadoc이 생성자는 알아서 위에다가 정리해주는데 정적 팩토리 메서드에는 그런걸 안해준다. 따라서 API 문서를 잘 작성하고 널리 알려진
메서드 네이밍 컨벤션을 사용해서 클라이언트가 잘 사용할 수 있게 해줘야한다(이름을 잘 짓자!).

다음은 정적 팩토리 메서드에 흔히 사용하는 명명 방식들이다.

* from : 매개 변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드
    - ex) Date d = Date.from(instance);
* of : 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드
    - ex) Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
* valueOf : from과 of의 더 자세한 버전
    - ex) BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE)
    -     Integer integer = Integer.valueOf("35");
* instance 혹은 getInstance : (매개변수를 받는다면) 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.
    - ex) StackWalker luke = StackWalker.getInstance(options);
* create 혹은 newInstance : instance 혹은 getInstance와 같지만, 매번 새로운 인스턴스가 보장됨을 반환한다.
* get*Type* : getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩토리 메서드를 정의할 때 쓴다. "Type"은 팩토리 메서드가 반환할 객체의 타입이다.
    - ex) FileStore fs = Files.getFileStore(path);
* new*Type* : newInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩토리 메서드를 정의할 때 쓴다. "Type"은 팩토리 메서드가 반환할 객체의 타입이다.
    - ex) BufferedReader br = Files.newBufferedReader(path);
* *type* : get*Type*과 new*Type*의 간결한 버전
    - ex) List<Complaint> litany = Collections.list(legacyLitany);
