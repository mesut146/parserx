package mesut.parserx.dfa;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Range;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

//character classes used by nfa,dfa
public class Alphabet {
    public static int min = Character.MIN_VALUE;//0x0000
    public static int max = Character.MAX_VALUE;//0xFFFF
    private final Map<Integer, Node> reverseMap = new HashMap<>();
    public Map<Node, Integer> map = new HashMap<>();
    public int lastId = 0;

    public int size() {
        return map.size();
    }

    public int addRegex(Node node) {
        if (node.isRange()) {
            check(node.asRange());
        }
        if (!map.containsKey(node)) {
            map.put(node, lastId);
            reverseMap.put(lastId, node);
            lastId++;
            return lastId - 1;
        }
        return map.get(node);
    }

    //check if range conflicts with existing ranges
    private void check(Range range) {
        Range r = findRange(range);
        if (r != null && !r.equals(range)) {
            throw new RuntimeException("conflicting range in alphabet: " + range + " on " + r);
        }
    }

    public int getId(Node node) {
        if (map.containsKey(node)) {
            return map.get(node);
        }
        throw new RuntimeException("invalid range " + node);
    }

    public Range getRange(int id) {
        return getRegex(id).asRange();
    }

    public Node getRegex(int id) {
        if (reverseMap.containsKey(id)) {
            return reverseMap.get(id);
        }
        throw new RuntimeException("invalid input id: " + id);
    }

    //find intersecting range
    public Range findRange(Range range) {
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
            w.printf("%s -> %s\n", getRegex(id), id);
        }
        w.close();
    }
}
