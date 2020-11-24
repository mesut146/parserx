package nodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

//can be lexer group or parser group
//(rule1 rule2)
public class GroupNode extends Node implements Iterable<Node> {

    public Node rhs;

    public GroupNode(Node rhs) {
        this.rhs = rhs;
    }

    public GroupNode() {
    }

    public String toString() {
        return "(" + rhs + ")";
    }

    @Override
    public Iterator<Node> iterator() {
        if (rhs instanceof NodeList) {
            return ((NodeList) rhs).iterator();
        }
        return Collections.singletonList(rhs).iterator();
    }
}
