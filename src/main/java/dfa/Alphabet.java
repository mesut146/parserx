package dfa;

import nodes.RangeNode;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Alphabet {
    public Map<RangeNode, Integer> map = new HashMap<>();
    int lastId = 0;

    public Map<RangeNode, Integer> getMap() {
        return map;
    }

    public void add(RangeNode rangeNode) {
        check(rangeNode);
        if (!map.containsKey(rangeNode)) {
            map.put(rangeNode, lastId++);
        }
    }

    private void check(RangeNode rangeNode) {
        RangeNode r = findRange(rangeNode);
        if (r != null && !r.equals(rangeNode)) {
            throw new RuntimeException("conflicting range in alphabet: " + rangeNode + " on " + r);
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
        throw new RuntimeException("invalid range id: " + id);
    }

    public Iterator<RangeNode> getRanges() {
        return map.keySet().iterator();
    }

    public RangeNode findRange(int ch) {
        for (Map.Entry<RangeNode, Integer> entry : map.entrySet()) {
            if (entry.getKey().start <= ch && entry.getKey().end >= ch) {
                return entry.getKey();
            }
        }
        return null;
    }

    public RangeNode findRange(RangeNode range) {
        for (Map.Entry<RangeNode, Integer> entry : map.entrySet()) {
            if (entry.getKey().intersect(range)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void dump(File file) {
        OutputStream os = System.out;
        if (file != null) {
            try {
                os = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        PrintWriter w = new PrintWriter(os);
        for (int id = 0; id < lastId; id++) {
            w.printf("%s -> %s\n", getRange(id), id);
        }
        w.close();
        System.out.println("alphabet dumped to " + file);
    }
}
