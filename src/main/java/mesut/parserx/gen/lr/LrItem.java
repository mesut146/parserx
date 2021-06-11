package mesut.parserx.gen.lr;

import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//lr0,lr1
public class LrItem {
    public List<NameNode> lookAhead = new ArrayList<>();
    public RuleDecl ruleDecl;
    public int dotPos;
    public LrItemSet gotoSet;

    public LrItem(RuleDecl ruleDecl, int dotPos) {
        this.ruleDecl = ruleDecl;
        this.dotPos = dotPos;
    }

    public LrItem(LrItem ruleDecl, int dotPos) {
        this(ruleDecl.ruleDecl, dotPos);
        this.lookAhead = new ArrayList<>(ruleDecl.lookAhead);
    }

    public boolean hasReduce() {
        //if dot at end we are reducing
        return getDotNode() == null;
    }

    boolean isLr0() {
        return lookAhead.isEmpty();
    }

    public boolean isKernel() {
        return dotPos == 0;
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
        if (!isLr0()) {
            sb.append(" , ");
            sb.append(NodeList.join(lookAhead, "/"));
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
        if (dotPos < rhs.size()) {
            return rhs.get(dotPos).asName();
        }
        return null;
    }

    //2 node after dot
    public NameNode getDotNode2() {
        Sequence rhs = ruleDecl.rhs.asSequence();
        if (dotPos < rhs.size() - 1) {
            return rhs.get(dotPos + 1).asName();
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        LrItem item = (LrItem) other;

        if (dotPos != item.dotPos) return false;
        return Objects.equals(ruleDecl, item.ruleDecl) && Objects.equals(lookAhead, item.lookAhead);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleDecl, dotPos, lookAhead);
    }
}
