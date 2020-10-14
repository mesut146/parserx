package nodes;

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

}
