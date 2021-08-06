package Chapter2.item6;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
