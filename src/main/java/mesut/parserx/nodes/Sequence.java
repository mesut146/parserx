package mesut.parserx.nodes;

import java.util.Arrays;
import java.util.List;

public class Sequence extends NodeList {

    public static boolean hasSpace = true;

    public Sequence(Node... arr) {
        this(Arrays.asList(arr));
    }

    public Sequence(List<Node> arr) {
        super(arr);
    }

    public static Node of(Node... a) {
        return new Sequence(a).normal();
    }

    @Override
    public String toString() {
        return NodeList.join(list, hasSpace ? " " : "");
    }

    @Override
    public Node normal() {
        normalCh();
        if (size() == 1) {
            return first();
        }
        Sequence s = new Sequence();
        for (Node ch : this) {
            if (ch.isSequence()) {
                s.addAll(ch.asSequence().list);
            }
            else if (ch.isOr()) {
                s.add(new Group(ch));
            }
            else if (!s.isEpsilon()) {
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
