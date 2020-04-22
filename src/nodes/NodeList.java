package nodes;

import java.util.ArrayList;
import java.util.List;

public class NodeList<T> extends Node {
    public List<T> list = new ArrayList<>();
    
    /*public void add(T node){
        list.add(node);
    }*/

    public <E extends T> void add(E node) {
        list.add(node);
    }

    public void addAll(List<T> other) {
        list.addAll(other);
    }

    public void addAll(NodeList<T> other) {
        list.addAll(other.list);
    }

    public T get(int index) {
        return list.get(index);
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

    public String join(String del) {
        return join(list, del);
    }


}
