package gen;

import nodes.Node;
import nodes.OrNode;
import nodes.Sequence;
import rule.RuleDecl;

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

    public Node getDotNode() {
        Node rhs = ruleDecl.rhs;
        if (rhs.isName()) {
            if (dotPos == 0) {
                return rhs;
            }
            return null;
        }
        else if (rhs.isSequence()) {
            Sequence sequence = rhs.asSequence();

            if (dotPos == sequence.list.size()) {
                return null;
            }
            return sequence.list.get(dotPos);
        }
        return null;
    }
}
