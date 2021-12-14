package mesut.parserx.nodes;

import java.util.Arrays;
import java.util.List;

public class Sequence extends NodeList {

    public boolean assocLeft, assocRight;

    public Sequence(Node... arr) {
        this(Arrays.asList(arr));
    }

    public Sequence(List<Node> arr) {
        super(arr);
        for (Node ch : arr) {
            if (ch.isOr()) {
                throw new RuntimeException("invalid child, wrap using group");
            }
        }
    }

    public static Node wrap(Node node) {
        if (node.isSequence()) {
            return new Group(node);
        }
        return node;
    }

    public static Sequence make(Node a, Node b) {
        return new Sequence(Or.wrap(a), Or.wrap(b));
    }

    public static Node make(List<Node> list) {
        if (list.size() == 1) return list.get(0);
        return new Sequence(list);
    }

    @Override
    public String toString() {
        return NodeList.join(list, " ");
    }

    @Override
    public Node normal() {
        normalCh();
        if (size() == 1) {
            if (astInfo.which != -1) {
                throw new RuntimeException("norm with code");
            }
            Node res = get(0);
            res.label = label;
            return res;
        }
        Sequence res = new Sequence();
        for (Node ch : this) {
            if (ch.isSequence()) {
                res.addAll(ch.asSequence().list);
            }
            else {
                res.add(ch);
            }
        }
        res.astInfo = astInfo.copy();
        res.assocLeft = assocLeft;
        res.assocRight = assocRight;
        return res;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitSequence(this, arg);
    }
}
