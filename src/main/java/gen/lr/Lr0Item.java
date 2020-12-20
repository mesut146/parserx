package gen.lr;

import nodes.NameNode;
import nodes.RuleDecl;
import nodes.Sequence;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ruleDecl.name);
        sb.append(" -> ");
        Sequence rhs = ruleDecl.rhs.asSequence();
        for (int i = 0; i < rhs.size(); i++) {
            if (i == dotPos) {
                sb.append(". ");
            }
            sb.append(rhs.get(i));
            if (i < rhs.size() - 1) {
                sb.append(" ");
            }
        }
        if (rhs.size() == dotPos) {
            sb.append(".");
        }
        return sb.toString();
    }

    //if dot follows a terminal
    public boolean isDotNonTerminal() {
        NameNode nameNode = getDotNode();
        if (nameNode == null) {
            return false;
        }
        return !nameNode.isToken;
    }

    //node after dot
    public NameNode getDotNode() {
        Sequence rhs = ruleDecl.rhs.asSequence();
        if (dotPos2 < rhs.size()) {
            return rhs.get(dotPos2).asName();
        }
        return null;
    }

    //node 2after dot
    public NameNode getDotNode2() {
        Sequence rhs = ruleDecl.rhs.asSequence();
        if (dotPos2 < rhs.size() - 1) {
            return rhs.get(dotPos2 + 1).asName();
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
