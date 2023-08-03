package mesut.parserx.utils;

import java.util.HashMap;
import java.util.Map;

public class CountingMap<K> {
    Map<K, Integer> map = new HashMap<>();

    public int get(K key) {
        if (map.containsKey(key)) {
            int i = map.get(key);
            i++;
            map.put(key, i);
            return i;
        } else {
            map.put(key, 1);
            return 1;
        }
    }
}
