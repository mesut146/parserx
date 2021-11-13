package mesut.parserx.nodes;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

//can be lexer group or parser group
//(rule1 rule2)
public class Group extends Node implements Iterable<Node> {

    public Node node;

    public Group(Node rhs) {
        this.node = rhs;
    }

    public String toString() {
        boolean backup = Or.newLine;
        Or.newLine = false;
        String s = varString() + "(" + node + ")";
        Or.newLine = backup;
        return s;
    }

    @Override
    public Iterator<Node> iterator() {
        if (node instanceof NodeList) {
            return ((NodeList) node).iterator();
        }
        return Collections.singletonList(node).iterator();
    }

    public Node normal() {
        if (node.isSequence() || node.isOr()) {
            return this;
        }
        //extract simple nodes
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group nodes = (Group) o;
        return Objects.equals(node, nodes.node);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitGroup(this, arg);
    }
}
