package mesut.parserx.utils;

import java.util.HashMap;
import java.util.Map;

public class CountingMap2<K1, K2> {
    Map<K1, CountingMap<K2>> map = new HashMap<>();

    public int get(K1 key1, K2 key2) {
        CountingMap<K2> c = map.get(key1);
        if (c == null) {
            c = new CountingMap<>();
            map.put(key1, c);
        }
        return c.get(key2);
    }
}
