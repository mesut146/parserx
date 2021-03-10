package nodes;

import java.util.Collections;
import java.util.Iterator;

//can be lexer group or parser group
//(rule1 rule2)
public class GroupNode extends Node implements Iterable<Node> {

    public Node node;

    public GroupNode(Node rhs) {
        this.node = rhs;
    }

    public String toString() {
        return "(" + node + ")";
    }

    @Override
    public Iterator<Node> iterator() {
        if (node instanceof NodeList) {
            return ((NodeList) node).iterator();
        }
        return Collections.singletonList(node).iterator();
    }

    public Node normal() {
        if (node.isString() || node.isBracket() || node.isName() || node.isRegex() || node.isGroup()) {
            return node;
        }
        return this;
    }
}
