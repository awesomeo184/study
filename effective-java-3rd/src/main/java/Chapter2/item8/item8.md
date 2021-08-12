# Finalizer와 Cleaner는 사용하지 마라

Class는 생성자(Constructor)와 소멸자(Destructor)라는 특별한 함수가 있다. 생성자는 익숙한 개념이지만 자바에서 소멸자를 직접 정의하는 일은 흔치 않기 때문에 익숙하지 않을 수 있는데,
그냥 생성자의 반대 개념이다. 객체가 소멸될 때 호출되는 함수이다.

자바는 두 가지 객체 소멸자 `finalizer`와 `cleaner`를 제공한다. 우선 **결론부터 말하자면 finalizer는 쓰면 안된다.** 언제 실행될지 예측할 수 없고 상황에 따라 사용하면 위험할 수 있다. 그래서 Java9에서는 deprecated되고 그 대안으로 cleaner가 새로 추가됐다.
그렇지만 **cleaner도 finalizer보다는 덜 위험하지만 여전히 예측할 수 없고 일반적으로 불필요하다.**

> 책에 따르면 finalizer와 cleaner는 C++의 소멸자(destructor)와는 다른 개념이라고 한다.
> C++에서는 소멸자를 통해 특정 객체와 관련된 자원을 회수하는 것이 보편적이며, 비메모리 자원도 소멸자를 통해 회수한다고한다. 
> 근데 자바는 메모리 관리를 GC가 대신해주고 자원 회수는 try-with-resources나 try-finally로 하면 된다. 

finalizer가 언제 수행될지 예측할 수 없다는 것은 코드로 직접 확인해볼 수 있다. `Object` 클래스의 finalize() 메서드를 오버라이드해 GC가 일어날 때 할 행동을 정의할 수 있다.

```java
public class FinalizerDemo {

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Clean up");
    }

    public void hello() {
        System.out.println("hello");
    }
}
```

```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
        Thread.sleep(1000);
    }

    private void run() {
        FinalizerDemo finalizerDemo = new FinalizerDemo();
        finalizerDemo.hello();
    }
}
```

`FinalizerDemo`의 인스턴스에 대한 GC가 수행될 때 "Clean Up"이라는 문자열을 출력하도록 finalize() 메서드를 오버라이드 한 뒤, `Main` 클래스에 클라이언트 코드를 작성했다.
예측하기로는 run 메서드가 종료되면 더이상 `FinalizerDemo`의 인스턴스에 대한 참조가 존재하지 않기 때문에 GC가 수행될 것이고 메인 스레드가 1초를 기다리는 사이에 "Clean Up"이 출력되어야한다.

하지만 실제로 코드를 돌려보면 finalize()는 호출되지 않는다. 아래처럼 GC가 일어날만한 충분한 조건을 만들어보면 간헐적으로 "Clean Up"이 출력되는 것을 확인할 수 있다.
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
    }

    private void run() {
        for (int i = 0; i < 1000000; i++) {   // 인스턴스 생성과 소멸을 100만번 반복
            FinalizerDemo finalizerDemo = new FinalizerDemo();
            finalizerDemo.hello();
        }
    }
}
```

출력 결과
```
...
hello
hello
hello
Clean Up
Clean Up
Clean Up
Clean Up
Clean Up
...
```

자바 언어 명세는 finalizer나 cleaner의 수행 시점뿐 아니라 수행 여부조차 보장하지 않는다. 즉 객체에 딸린 종료 작업을 전혀 수행하지 못한 채 프로그램이 중단될 수 있다는 얘기다.
System.gc()나 System.runFinalization() 메서드를 호출해도 마찬가지이다. 실행될 가능성이 높아질 뿐이지 여전히 실행히 보장되지는 않는다.

finalizer와 cleaner에 의존적인 코드를 작성할 경우 일어날 수 있는 문제점에는 다음과 같은 것들이 있다.

## 쓰면 안되는 이유

1. 언제 실행될지 알 수 없다.
    * **따라서 타이밍이 중요한 작업을 절대로 finalizer나 cleaner에서 하면 안된다.** 예를 들어, 파일 리소스를 반납하는 작업을 그 안에서 처리한다면, 실제로 그 파일 리소스 처리가 언제 될지 알 수 없고, 자원 반납이 안되서 더이상 새로운 파일을 열 수 없는 상황이 발생할 수도 있다.
2. finalizer는 인스턴스 반납을 지연시킬 수 있다.
    * finalizer 스레드는 다른 애플리케이션 스레드보다 우선 순위가 낮다. **따라서 finalize 대기열에서 수천 개의 인스턴스가 반납되길 기다리다가 그대로 메모리가 뻗어버릴 수도 있다.** cleaner는 자신을 수행할 스레드를 제어할 수 있다는 측면에서 조금 낫긴하지만 여전히 즉각 수행되리라는 보장은 없다.
3. 아얘 실행이 안될 수 있다.
    * 위에서 직접 코드로 살펴본 것처럼 아얘 수행이 안되고 애플리케이션이 종료될 수도 있다. **따라서 절대로 finalizer나 cleaner로 상태를 영구적으로 변경하는 일(이를테면 DB 영구 락 해제)을 하면 안된다.** 데이터베이스 같은 자원의 락을 여기서 반환하는 작업을 한다면 전체 분산 시스템이 멈춰 버릴 수도 있다. 
4. 심각한 성능 문제가 발생할 수 있다.
    * AutoCloseable 객체를 만들고, try-with-resource로 자원 반납을 하는데 걸리는 시간은 12ns 인데 반해, Finalizer를 사용한 경우에 550ns, 약 50배가 걸렸다. Cleaner를 사용한 경우에는 66ns가 걸렸다 약 5배.
5. 보안 이슈
    * [finalize attack](https://self-learning-java-tutorial.blogspot.com/2020/03/finalizer-attack-in-java.html)이라는 보안 이슈에 이용될 수 있다.

## 그럼 언제 쓰는가?

cleaner(java8 이전까진 finalizer)는 **자원 반납의 안전망 역할**이나 **네이티브 자원 정리** 외에는 사용하지 않는다.

### 자원 반납의 안전망(safety-net)

자원을 반납하는 정석적인 방법은 클라이언트에서 try-with-resources(java 7)나 try-finally로 반납하는 것이다. 클라이언트에서 자원을 반환하지 않았을 경우에 대비한 안전망으로써 finalizer나 cleaner를 작성할 수 있다.
실제로 자바에서 제공하는 FileInputStream, FileOutputStream, ThreadPoolExecutor 그리고 java.sql.Connection에는 안전망으로 동작하는 finalizer가 있다.

### 네이티브 피어 정리

```
자바 클래스 -> 네이티브 메소드 호출 -> 네이티브 객체 (네티이브 Peer)
```

네이티브 객체는 일반적인 객체가 아니라서 GC가 그 존재를 모른다. 따라서 네이티브 피어가 들고 있는 리소스를 Cleaner나 Finalizer를 사용해서 해당 자원을 반납할 수도 있다(책에서는 "해당 자원이 크게 중요하지 않으며, 성능상 영향이 크지 않다면"이라는 단서 조항을 달아놓았다).
