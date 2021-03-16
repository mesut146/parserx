package nodes;

import java.util.Iterator;
import java.util.List;

//list of rules
//rhs
public class Sequence extends NodeList {

    public Sequence(Node... arr) {
        super(arr);
    }

    public Sequence(List<Node> arr) {
        super(arr);
    }

    public static Sequence of(Node... a) {
        return new Sequence(a);
    }

    @Override
    public String toString() {
        return NodeList.join(list, " ");
    }

    public Node normal() {
        if (size() == 1) {
            return first();
        }
        Sequence s = new Sequence();
        for (Node ch : this) {
            if (ch.isSequence()) {
                s.addAll(ch.asSequence().list);
            }
            else {
                s.add(ch);
            }
        }
        return s;
    }

    @Override
    public Node copy() {
        return new Sequence(list);
    }
}
