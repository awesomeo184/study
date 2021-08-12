# 다 쓴 객체 참조를 해제하라

새삼스럽지만 자바는 가비지 컬렉터가 메모리 관리를 자동으로 해준다. 근데 이때문에 프로그래머가 메모리 관리에 더 이상 신경쓰지 않아도 된다고 오해할 수 있는데 그렇지 않다.
메모리 누수를 주의해야하는 케이스는 다음과 같은 것들이 있다.

## 클래스가 메모리를 직접 관리

아래의 코드에서는 메모리 누수가 일어난다.

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object object) {
        ensureCapacity();
        elements[size++] = object;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];  // 이 부분이 문제
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

스택에 계속 값을 쌓았다가 빼냈다고 치자. 그래도 스택이 차지하고 있는 메모리는 줄지 않는다. 왜냐하면 스택의 구현체가 필요없는 객체에 대한 레퍼런스를 그대로 갖고 있기 때문이다.

아래와 같이 pop() 메서드를 수정할 수 있다.
```java
    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        Object value = this.elements[--size];
        this.elements[size] = null;
        return value;
    }
```

스택에서 값을 꺼내고 그 위치를 null로 설정해주면 다음 GC가 일어날 때, 레퍼런스가 정리된다. 실수로 해당 위치의 객체에 다시 접근하려고 할 때 `NullPointerException`이 발생할 수 있지만 그 자리에 있는 객체를 비우지 않고 실수로 잘못된 객체를 돌려주는 것 보다는 낫다.
발견하기 힘든 버그를 찾기 위해 코드를 샅샅히 뒤지는 것보다는 차라리 프로그램이 에러를 던져주는 편이 훨씬 낫기 때문이다.

그렇다고 필요없는 객체를 볼 때마다 null로 설정해줄 필요는 없다. **객체를 null로 설정하는 건 예외적인 상황에서나 하는것이지 평범한 일은 아니다.** 
그럼 언제 null로 설정해주느냐, 위처럼 클래스 내에서 메모리 관리를 해주는 경우에 해준다. `Stack` 구현체처럼 `elements`라는 배열을 내부에서 직접 관리하는 경우에 GC는 어떤 객체가 필요없는 객체인지 알 수 없다.
따라서 해당 레퍼런스를 null로 만들어서 GC에게 필요없는 부분이라는 것을 알려주어야한다.

일반적인 경우에 필요없는 객체의 레퍼런스를 관리하는 방법은 **변수를 가능한 가장 최소의 스코프에서 사용하는 것이다.** 

## 캐시

캐시 역시 메모리 누수를 일으키는 주범이다. 객체 참조를 캐시에 넣어두고, 이 사실을 까맣게 잊은 채 그 객체를 다 쓴 뒤로도 한참을 그냥 놔두는 경우를 자주 접할 수 있다.

**캐시 외부에서 key를 참조하는 동안만 엔트리가 살아있는 캐시가 필요한 상황**이라면 `WeakHashMap` 사용을 고려해볼 수 있다.
```java
    public static void main(String[] args) {
        Object key1 = new Object();  // Strong reference
        Object value1 = new Object();

        Map<Object, Object> cache = new WeakHashMap<>();
        cache.put(key1, value1);   // key가 weak reference로 관리된다. Strong reference의 참조가 사라지면 자동으로 해제된다.
    }
```

weak reference에 대한 내용은 [이 글](https://web.archive.org/web/20061130103858/http://weblogs.java.net/blog/enicholas/archive/2006/05/understanding_w.html)을 참조.

또는 시간이 지날수록 엔트리의 가치를 떨어뜨리는 방식을 사용해서 쓰지 않는 엔트리를 주기적으로 청소해주는 백그라운드 쓰레드(`ScheduledThreadPoolExecutor`)를 활용하거나
새로운 엔트리를 추가할 때 부수적인 작업으로 기존 캐시를 비우는 작업을 해줄 수 있다.(`LinkedHashMap` 클래스는 `removeEldestEntry`라는 메서드를 써서 이 방식으로 처리한다.)
더 복잡한 캐시를 만들고 싶다면 java.lang.ref 패키지를 직접 활용해야한다.

## 콜백

메모리 누수의 다른 주범으로는 콜백(or 리스너)이 있다. 클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면, 뭔가 조치를 취하지 않는 한 콜백은 계속 쌓인다. 이럴 때 콜백을 weak reference로 저장하면 가비지 컬렉터가 즉시 수거해간다.