package nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// rule1 | rule2 | rule3...
public class OrNode extends Node implements Iterable<Node> {

    //seq,single
    public List<Node> list = new ArrayList<>();

    public OrNode() {
    }

    public OrNode(Node... args) {
        list.addAll(Arrays.asList(args));
    }

    public OrNode(List<Node> args) {
        list.addAll(args);
    }

    public void add(Node rule) {
        list.add(rule);
    }


    //print with bars
    String normal() {
        return NodeList.join(list, " | ");
    }

    @Override
    public String toString() {
        return normal();
    }


    @Override
    public Iterator<Node> iterator() {
        return list.iterator();
    }
}
