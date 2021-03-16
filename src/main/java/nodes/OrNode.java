package nodes;

import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.List;

// rule1 | rule2 | rule3...
public class OrNode extends NodeList {

    public OrNode(Node... args) {
        super(args);
    }

    public OrNode(List<Node> args) {
        super(args);
    }

    //print with bars
    String printNormal() {
        return NodeList.join(list, " | ");
    }

    @Override
    public String toString() {
        return printNormal();
    }

    public Node normal() {
        if (size() == 1) {
            return first();
        }
        OrNode s = new OrNode();
        for (Node ch : this) {
            if (ch.isOr()) {
                s.addAll(ch.asOr().list);
            }
            else {
                s.add(ch);
            }
        }
        return s;
    }

    @Override
    public Node copy() {
        return new OrNode(list);
    }
}
