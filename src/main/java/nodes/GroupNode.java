package nodes;

import java.util.Iterator;

//can be lexer group or parser group
//(rule1 rule2)
//T is Node
public class GroupNode<T extends Node> extends Node implements Iterable<Node> {

    public T rhs;

    public GroupNode() {
    }

    public GroupNode(T rhs) {
        this.rhs = rhs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Iterator<Node> iterator() {
        if (rhs.isSequence()) {
            return rhs.asSequence().iterator();
        }
        return new Iterator<Node>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos == 0;
            }

            @Override
            public Node next() {
                pos++;
                return rhs;
            }

            @Override
            public void remove() {

            }
        };
    }
}
