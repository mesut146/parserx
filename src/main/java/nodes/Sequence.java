package nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//list of rules
//rhs
public class Sequence extends Node implements Iterable<Node> {

    public List<Node> list = new ArrayList<>();

    public Sequence(Node... arr) {
        list.addAll(Arrays.asList(arr));
    }

    public void add(Node rule) {
        list.add(rule);
    }

    public Node normal() {
        if (list.size() == 1) {
            return list.get(0);
        }
        return this;
    }

    @Override
    public String toString() {
        return NodeList.join(list, " ");
    }


    @Override
    public Iterator<Node> iterator() {
        return list.iterator();
    }
}
