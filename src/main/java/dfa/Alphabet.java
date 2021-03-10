package dfa;

import nodes.Node;
import nodes.RangeNode;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Alphabet {
    public Map<Node, Integer> map = new HashMap<>();
    int lastId = 0;

    public int size() {
        return map.size();
    }

    public void add(RangeNode rangeNode) {
        check(rangeNode);
        if (!map.containsKey(rangeNode)) {
            map.put(rangeNode, lastId++);
        }
    }

    public int addRegex(Node node) {
        //check(node);
        if (!map.containsKey(node)) {
            map.put(node, lastId++);
        }
        return map.get(node);
    }

    public void add(List<RangeNode> rangeNodes) {
        for (RangeNode rangeNode : rangeNodes) {
            add(rangeNode);
        }
    }

    //check if range conflicts with existing ranges
    private void check(RangeNode rangeNode) {
        RangeNode r = findRange(rangeNode);
        if (r != null && !r.equals(rangeNode)) {
            throw new RuntimeException("conflicting range in alphabet: " + rangeNode + " on " + r);
        }
    }


    public int getId(Node rangeNode) {
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
        return getRegex(id).asRange();
    }

    public Node getRegex(int id) {
        for (Map.Entry<Node, Integer> entry : map.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("invalid range id: " + id);
    }

    public void update(int id, Node node) {
        map.remove(getRegex(id));
        map.put(node, id);
    }

    public Iterator<RangeNode> getRanges() {
        final Iterator<Node> iterator = map.keySet().iterator();
        return new Iterator<RangeNode>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public RangeNode next() {
                return iterator.next().asRange();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    //find intersecting range
    public RangeNode findRange(RangeNode range) {
        for (Map.Entry<Node, Integer> entry : map.entrySet()) {
            if (entry.getKey().asRange().intersect(range)) {
                return entry.getKey().asRange();
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