package mesut.parserx.nodes;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

//can be lexer group or parser group
//(rule1 rule2)
public class GroupNode extends Node implements Iterable<Node> {

    public Node node;

    public GroupNode(Node rhs) {
        this.node = rhs;
    }

    public String toString() {
        return varString() + "(" + node + ")";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupNode nodes = (GroupNode) o;
        return Objects.equals(node, nodes.node);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    @Override
    public Node copy() {
        return new GroupNode(node);
    }
}
