package mesut.parserx.nodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// rule1 | rule2 | rule3...
public class Or extends NodeList {

    public static boolean newLine = false;

    public Or(Node... args) {
        this(Arrays.asList(args));
    }

    public Or(List<Node> args) {
        super(args);
        for (Node ch : args) {
            if (ch.isOr()) {
                //throw new RuntimeException("invalid child, wrap using group");
            }
        }
    }

    public static Node wrap(Node node) {
        if (node.isOr()) {
            return new Group(node);
        }
        return node;
    }

    public static Or make(Node a, Node b) {
        return new Or(Sequence.wrap(a), Sequence.wrap(b));
    }

    public static Node make(List<Node> list) {
        if (list.size() == 1) return list.get(0);
        return new Or(list);
    }

    public String withNewline() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Iterator<Node> it = list.iterator(); it.hasNext(); ) {
            Node node = it.next();
            sb.append("   ");
            if (first) {
                sb.append(" ");//to adjust bar
            }
            sb.append(node.withLabel());
            sb.append("\n");
            if (it.hasNext()) {
                sb.append("|");
            }
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Node> it = list.iterator(); it.hasNext(); ) {
            Node node = it.next();
            sb.append(node);
            if (node.label != null) {
                sb.append(" #").append(node.label);
            }
            if (it.hasNext()) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    public Node normal() {
        normalCh();
        if (size() == 1) {
            return first().normal();
        }
        Or s = new Or();
        for (Node ch : this) {
            if (ch.isOr()) {
                s.addAll(ch.asOr().list);
            } else {
                s.add(ch);
            }
        }
        return s.dups();
    }

    //remove identical nodes
    public Or dups() {
        for (int i = 0; i < size(); i++) {
            Node ch = get(i);
            for (int j = i + 1; j < size(); j++) {
                if (ch.equals(get(j))) {
                    Or res = new Or(list);
                    res.list.remove(j);
                    return res.dups();
                }
            }
        }
        return this;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitOr(this, arg);
    }
}
