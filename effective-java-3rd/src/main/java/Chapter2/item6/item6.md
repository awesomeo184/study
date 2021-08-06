# 불필요한 객체 생성을 피하라

똑같은 기능의 객체를 매번 생성하기보다는 객체 하나를 재사용하는 것이 좋다.

## 문자열 객체

당연한 말이지만, String 객체는 반드시 리터럴로 선언하는 것이 좋다.

리터럴로 선언하면 Heap 영역의 스트링 풀에서 관리되기 때문에 같은 문자열을 여러 번 선언해도 객체는 한 번만 생성된다. 반면에 `new` 키워드로 선언하면 같은 형태의 문자열이라도 객체를 여러 번 생성한다.([관련글](https://wisdom-and-record.tistory.com/103))

```java
    public static void main(String[]args){
        String a = new String("Hello");
        String b = new String("Hello");
        String c = "Hello";
        String d = "Hello";

        System.out.println(a == b);  // false
        System.out.println(c == d);  // true
    }
```

## static 팩토리 메서드 사용하기

재사용 측면에서 `new Boolean(String)` 대신(Java9에서 deprecated 되었다) `Boolean.valueOf(String)`같은 static 팩토리 메서드를 쓰는 것이 좋다. 생성자는 반드시 객체를 생성해야하지만 static 팩토리 메서드는 그렇지 않다.

```java
    public static void main(String[] args) {
        Boolean v = Boolean.valueOf("true");
        System.out.println(v == Boolean.True)  //true
    }
```

## 비싼 객체

만드는데 메모리나 시간이 많이 드는 객체를 "비싼 객체"라고 부르는데, 이런 객체는 가급적 한 번만 만들고 재사용하는 방안을 강구하는게 좋다.

대표적인 예로 정규표현식의 `Pattern` 객체가 있다.

```java
    static boolean isRomanNumeral(String s) {
        return s.matches("^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
    }
```

**`String.matches()`는 정규표현식으로 문자열을 확인하는 가장 간편한 방법이지만, 성능이 중요한 상황에서 반복적으로 사용하기에는 적절하지 않다.**

`String.matches`는 내부적으로 `Pattern` 객체를 만들어 쓰는데 입력받은 정규표현식에 해당하는 유한 상태 기계를 만드는데다 한번 쓰고나면 GC의 대상이된다. 따라서 `Pattern` 객체를 따로 만들어서 재사용하는 것이 좋다.

```java
public class RomanNumber {

    private static final Pattern ROMAN = Pattern.compile("^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

    static boolean isRomanNumeral(String s) {
        return ROMAN.matcher(s).matches();
    }
}
```

## 어댑터

객체가 불변이라면 재사용해도 안전함이 명백하다. 그런데 훨씬 덜 명백하거나 오히려 직관에 반대되는 경우도 있다.

어댑터의 경우 실제 작업은 뒷단의 객체에 위임하고 자신은 인터페이스 역할만 하기 때문에 뒷단 객체 하나당 어댑터 하나만 생성하면된다.

그래서 `Map` 인터페이스의 `keySet()`의 경우 keySet을 호출할 때마다 같은 객체를 리턴한다. 그렇기 때문에 리턴 받은 Set 타입의 객체를 변경하면 그 뒤에 있는 Map 객체도 변경된다.

```java
public class KeySetDemo {

    public static void main(String[] args) {
        Map<Long, String> members = new HashMap<>();
        members.put(1L, "member1");
        members.put(2L, "member2");

        Set<Long> set1 = members.keySet();
        Set<Long> set2 = members.keySet();

        System.out.println(set1 == set2); // true

        set1.remove(1L);
        System.out.println(set2.size()); // 1
        System.out.println(members.size()); // 1
    }
}
```

## 오토박싱

불필요한 객체 생성을 피하는 또 한 가지 방법은 오토박싱을 신중하게 사용하는 것이다.

**오토박싱은 프리미티브 타입과 그에 대응하는 방싱된 기본 타입의 구분을 흐려주지만, 그 경계를 완전히 없애는 것은 아니다.**

```java
public class AutoBoxingDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Long sum = 0l;
        for (long i = 0 ; i <= Integer.MAX_VALUE ; i++) {
            sum += i;
        }
        System.out.println(sum);
        System.out.println(System.currentTimeMillis() - start);
    }
}
```

위 코드에서 `sum` 변수의 타입을 Long으로 만들었기 때문에 불필요한 Long 객체를 2의 31 제곱개 만큼 만들게 되고 대략 6초 조금 넘게 걸린다. 타입을 프리미티브 타입으로 바꾸면 600 밀리초로 약 10배 이상의 차이가 난다.

따라서 **박싱된 기본 타입보다는 프리미티브 타입을 사용하고 불필요한 오토박싱이 코드에 침투하지 않도록 주의해야한다.**
