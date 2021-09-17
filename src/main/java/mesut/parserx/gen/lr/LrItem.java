package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//lr0,lr1
public class LrItem {
    public Set<Name> lookAhead = new HashSet<>();
    public RuleDecl rule;
    public int dotPos;
    public Set<LrItemSet> gotoSet = new HashSet<>();
    int hash = -1;

    public LrItem(RuleDecl rule, int dotPos) {
        this.rule = rule;
        this.dotPos = dotPos;
    }

    public LrItem(LrItem item, int dotPos) {
        this(item.rule, dotPos);
        this.lookAhead = new HashSet<>(item.lookAhead);
    }

    public boolean hasReduce() {
        //if dot at end we are reducing
        return getDotNode() == null;
    }

    boolean isLr0() {
        return lookAhead.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rule.name);
        sb.append(" -> ");
        Sequence rhs = rule.rhs.asSequence();
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
            sb.append(NodeList.join(new ArrayList<>(lookAhead), "/"));
        }
        return sb.toString();
    }

    //if dot follows a terminal
    public boolean isDotNonTerminal() {
        Name name = getDotNode();
        return name == null ? false : !name.isToken;
    }

    //node after dot
    public Name getDotNode() {
        Sequence rhs = rule.rhs.asSequence();
        if (dotPos < rhs.size()) {
            return rhs.get(dotPos).asName();
        }
        return null;
    }

    //2 node after dot
    public Name getDotNode2() {
        Sequence rhs = rule.rhs.asSequence();
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

        //if (hash == item.hash) return true;
        if (dotPos != item.dotPos) return false;
        return Objects.equals(rule, item.rule) && lookAhead.equals(item.lookAhead);
    }

    //without lookahead
    public boolean isSame(LrItem other) {
        return dotPos == other.dotPos && Objects.equals(rule, other.rule);
    }

    @Override
    public int hashCode() {
        if (hash == -1)
            hash = Objects.hash(rule, dotPos, lookAhead);
        return hash;
    }
}
