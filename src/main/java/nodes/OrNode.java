package nodes;

import java.util.Arrays;
import java.util.Iterator;

// rule1 | rule2 | rule3...
public class OrNode extends Node implements Iterable<Node> {

    //seq,single
    public NodeList<Node> list = new NodeList<>();

    public OrNode() {
    }

    public OrNode(Node... args) {
        list.addAll(Arrays.asList(args));
    }

    public void add(Node rule) {
        list.add(rule);
    }


    /*String array() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(list.join(""));
        sb.append("]");
        return sb.toString();
    }*/

    //print with bars
    String normal() {
        return list.join(" | ");
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
