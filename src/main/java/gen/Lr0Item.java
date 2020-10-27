package gen;

import nodes.NameNode;
import nodes.Node;
import nodes.Sequence;
import nodes.RuleDecl;

import java.util.Objects;

public class Lr0Item {
    RuleDecl ruleDecl;
    int dotPos;//orig
    int dotPos2;//current

    public Lr0Item(RuleDecl ruleDecl, int dotPos) {
        this.ruleDecl = ruleDecl;
        this.dotPos = dotPos;
        this.dotPos2 = dotPos;
    }

    public boolean isDotTerminal() {
        NameNode nameNode = getDotNode();
        if (nameNode == null) {
            return false;
        }
        return !nameNode.isToken;
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
            for (int i = 0; i < sequence.size(); i++) {
                if (i == dotPos) {
                    sb.append(". ");
                }
                sb.append(sequence.get(i));
                if (i < sequence.size() - 1) {
                    sb.append(" ");
                }
            }
            if (sequence.size() == dotPos) {
                sb.append(".");
            }
        }
        else if (rhs.isEmpty()) {
            if (dotPos == 0) {
                sb.append(". ");
            }
            sb.append(rhs);
            if (dotPos == 1) {
                sb.append(".");
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
            if (dotPos2 == 0) {
                return node.asName();
            }
            return null;
        }
        else if (node.isSequence()) {
            Sequence sequence = node.asSequence();

            if (dotPos2 == sequence.size()) {
                return null;
            }
            return sequence.get(dotPos2).asName();
        }
        else if (node.isEmpty()) {
            return null;
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
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
