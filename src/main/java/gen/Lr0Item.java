package gen;

import nodes.NameNode;
import nodes.Node;
import nodes.OrNode;
import nodes.Sequence;
import rule.RuleDecl;

import java.util.Objects;

public class Lr0Item {
    RuleDecl ruleDecl;
    int dotPos = 0;


    public Lr0Item(RuleDecl ruleDecl, int dotPos) {
        this.ruleDecl = ruleDecl;
        this.dotPos = dotPos;
    }

    public void step() {
        dotPos++;
    }

    public boolean isDotTerminal() {
        Node rhs = ruleDecl.rhs;
        if (rhs.isName()) {
            return dotPos == 0 && !rhs.asName().isToken;
        }
        else if (rhs.isSequence()) {
            Sequence sequence = rhs.asSequence();

            if (dotPos == sequence.list.size()) {
                return false;
            }
            return !sequence.list.get(dotPos).asName().isToken;
        }
        else if (rhs.isOr()) {
            OrNode orNode = rhs.asOr();
            if (dotPos == orNode.list.size()) {
                return false;
            }
            for (int i = 0; i < orNode.list.size(); i++) {
                Node or = orNode.list.get(i);
                if (i == dotPos) {
                    return or.isName() && !or.asName().isToken;
                }

            }
        }
        else {
            throw new RuntimeException("invalid node: " + rhs);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ruleDecl.name);
        sb.append(" -> ");
        Node rhs = ruleDecl.rhs;
        if (rhs.isName()) {
            if (dotPos == 0) {
                sb.append(". ");
            }
            sb.append(rhs);
            if (dotPos == 1) {
                sb.append(".");
            }
        }
        else if (rhs.isSequence()) {
            Sequence sequence = rhs.asSequence();
            for (int i = 0; i < sequence.list.size(); i++) {
                if (i == dotPos) {
                    sb.append(". ");
                }
                sb.append(sequence.list.get(i));
                if (i < sequence.list.size() - 1) {
                    sb.append(" ");
                }
            }
            if (sequence.list.size() == dotPos) {
                sb.append(".");
            }
        }
        else if (rhs.isOr()) {
            OrNode orNode = rhs.asOr();
            for (int i = 0; i < orNode.list.size(); i++) {
                sb.append(". ");
                sb.append(orNode.list.get(i));
                if (i < orNode.list.size() - 1) {
                    sb.append(" | ");
                }
            }
        }
        else {
            throw new RuntimeException("invalid node type: " + rhs);
        }
        return sb.toString();
    }

    public NameNode getDotNode() {
        return getDotNode(ruleDecl.rhs);
    }

    NameNode getDotNode(Node node) {
        if (node.isName()) {
            if (dotPos == 0) {
                return node.asName();
            }
            return null;
        }
        else if (node.isSequence()) {
            Sequence sequence = node.asSequence();

            if (dotPos == sequence.list.size()) {
                return null;
            }
            return sequence.list.get(dotPos).asName();
        }
        else if (node.isOr()) {
            OrNode orNode = node.asOr();
            for (int i = 0; i < orNode.list.size(); i++) {
                Node asd = orNode.list.get(i);
                Node dot = getDotNode(asd);
                if (dot != null) {
                    return asd.asName();
                }
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Lr0Item item = (Lr0Item) other;

        if (dotPos != item.dotPos) return false;
        return Objects.equals(ruleDecl, item.ruleDecl);
    }

    @Override
    public int hashCode() {
        int result = ruleDecl != null ? ruleDecl.hashCode() : 0;
        result = 31 * result + dotPos;
        return result;
    }
}
