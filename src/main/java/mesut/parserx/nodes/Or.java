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
    }

    String withNewline() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Node> it = list.iterator(); it.hasNext(); ) {
            Node node = it.next();
            sb.append(node.withLabel());
            sb.append("\n");
            if (it.hasNext()) {
                sb.append("|  ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean simp = isSimple();
        for (Iterator<Node> it = list.iterator(); it.hasNext(); ) {
            Node node = it.next();
            sb.append(node);
            if (node.label != null) {
                sb.append(" #").append(node.label);
            }
            if (newLine && !simp) {
                sb.append("\n  ");
            }
            if (it.hasNext()) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }

    //printable without newline
    boolean isSimple() {
        for (Node c : this) {
            if (!c.isString() && !c.isName()) {
                return false;
            }
        }
        return true;
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
            }
            else if (ch.isOptional()) {
                //doesn't make sense
            }
            else if (!ch.isEpsilon()) {
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
    public Node copy() {
        Or res = new Or(list);
        res.label = label;
        return res;
    }
}
