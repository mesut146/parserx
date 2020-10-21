package dfa;

import nodes.RangeNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Alphabet {
    public Map<RangeNode, Integer> map = new HashMap<>();
    int lastId = 0;

    public void add(RangeNode rangeNode) {
        if (!map.containsKey(rangeNode)) {
            map.put(rangeNode, lastId++);
        }
    }

    public int getId(RangeNode rangeNode) {
        if (!map.containsKey(rangeNode)) {
            throw new RuntimeException("invalid range " + rangeNode);
        }
        return map.get(rangeNode);
    }

    public int getId(int ch) {
        return getId(RangeNode.of(ch, ch));
    }

    public int getId(int left, int right) {
        return getId(RangeNode.of(left, right));
    }

    public RangeNode getRange(int id) {
        for (Map.Entry<RangeNode, Integer> entry : map.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("invalid range id");
    }

    public Iterator<RangeNode> getRanges() {
        return map.keySet().iterator();
    }

    public int findRange(int ch) {
        for (Map.Entry<RangeNode, Integer> entry : map.entrySet()) {
            if (entry.getKey().start <= ch && entry.getKey().end >= ch) {
                return entry.getValue();
            }
        }
        throw new RuntimeException("cant find range for " + (char) ch);
    }
}
