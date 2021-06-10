package mesut.parserx.nodes;

import java.util.Iterator;
import java.util.List;

// rule1 | rule2 | rule3...
public class OrNode extends NodeList {

    public static boolean newLine = true;
    public static int newLineLimit = 3;

    public OrNode(Node... args) {
        super(args);
    }

    public OrNode(List<Node> args) {
        super(args);
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
        normal0();
        if (size() == 1) {
            return first().normal();
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
        return new OrNode(list) {{
            this.label = label;
        }};
    }
}
