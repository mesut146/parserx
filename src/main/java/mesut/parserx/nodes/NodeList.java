package mesut.parserx.nodes;

import java.util.*;

public class NodeList extends Node implements Iterable<Node> {

    public List<Node> list = new ArrayList<>();

    public NodeList(List<Node> list) {
        this.list = new ArrayList<>(list);
    }

    public NodeList(Node... list) {
        this(new ArrayList<>(Arrays.asList(list)));
    }

    public NodeList() {
    }

    public static <T> String join(List<T> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            T node = list.get(i);
            if (node instanceof OrNode) {
                sb.append("(").append(node).append(")");
            }
            else {
                sb.append(node);
            }
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public static String join(int[] list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            sb.append(list[i]);
            if (i < list.length - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public int indexOf(Node node) {
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(node)) {
                return i;
            }
        }
        return -1;
    }

    public Node last() {
        return list.get(size() - 1);
    }

    public Node first() {
        return list.get(0);
    }

    public void add(Node node) {
        list.add(node);
    }


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
            return first();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeList nodes = (NodeList) o;

        return Objects.equals(list, nodes.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
