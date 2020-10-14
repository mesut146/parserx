package nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class NodeList extends Node implements Iterable<Node> {

    public List<Node> list;

    public NodeList(List<Node> list) {
        this.list = list;
    }

    public NodeList(Node... list) {
        this.list = new ArrayList<>(Arrays.asList(list));
    }

    public static <T> String join(List<T> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public void add(Node node) {
        list.add(node);
    }

    /*public void addAll(List<Node> other) {
        list.addAll(other);
    }*/

    public <T extends Node> void addAll(List<T> other) {
        list.addAll(other);
    }

    public Node get(int index) {
        return list.get(index);
    }

    public String join(String del) {
        return join(list, del);
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

    public Node normal() {
        if (list.size() == 1) {
            return list.get(0);
        }
        return this;
    }

    @Override
    public Iterator<Node> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }


    public void set(int i, Node node) {
        list.set(i, node);
    }
}
