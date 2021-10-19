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

    public static Sequence of(Node... a) {
        return new Sequence(a);
    }

    @Override
    public String toString() {
        return NodeList.join(list, hasSpace ? " " : "");
    }

    @Override
    public Node normal() {
        normalCh();
        if (size() == 1) {
            if (astInfo.which != -1) {
                throw new RuntimeException("norm with code");
            }
            return first();
        }
        Sequence res = new Sequence();
        for (Node ch : this) {
            if (ch.isSequence()) {
                res.addAll(ch.asSequence().list);
            }
            else if (ch.isOr()) {
                throw new RuntimeException();
            }
            else if (!ch.isEpsilon()) {
                res.add(ch);
            }
        }
        res.astInfo = astInfo.copy();
        return res;
    }
}
