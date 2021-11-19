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
            Node res = first();
            res.label = label;
            return res;
        }
        Sequence res = new Sequence();
        for (Node ch : this) {
            if (ch.isSequence()) {
                res.addAll(ch.asSequence().list);
            }
            else if (ch.isOr()) {
                throw new RuntimeException("invalid child");
            }
            else if (!ch.isEpsilon()) {
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
