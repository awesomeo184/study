package Chapter2.item7;

import java.util.Map;
import java.util.WeakHashMap;

public class CacheSample {

    public static void main(String[] args) {
        Object key1 = new Object();  // Strong reference
        Object value1 = new Object();

        Map<Object, Object> cache = new WeakHashMap<>();
        cache.put(key1, value1);   // key가 weak reference로 관리된다. Strong reference의 참조가 사라지면 자동으로 해제된다.
    }

}
