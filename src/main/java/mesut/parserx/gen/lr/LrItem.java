package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

//lr0,lr1
public class LrItem {
    public Set<Name> lookAhead = new HashSet<>();
    public RuleDecl rule;
    public int dotPos;
    public Set<LrItemSet> gotoSet = new HashSet<>();
    public Set<mesut.parserx.gen.lldfa.ItemSet> gotoSet2 = new HashSet<>();
    public boolean closured1 = false, closured2 = false;
    public LrItem sender;
    public Set<Integer> ids = new HashSet<>();
    int hash = -1;

    public LrItem(RuleDecl rule, int dotPos) {
        this.rule = rule;
        this.dotPos = dotPos;
        if (isEpsilon()) {
            //act as reduce
            this.dotPos = 1;
        }
    }

    public LrItem(LrItem item, int dotPos) {
        this(item.rule, dotPos);
        this.lookAhead = new HashSet<>(item.lookAhead);
        gotoSet2 = item.gotoSet2;
    }

    public static boolean isEpsilon(RuleDecl decl) {
        Sequence rhs = decl.rhs.asSequence();
        if (rhs.size() == 1) {
            return rhs.get(0).isEpsilon();
        }
        return false;
    }

    public boolean hasReduce() {
        //if dot at end we are reducing
        return getDotSym() == null;
    }

    boolean isLr0() {
        return lookAhead.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rule.ref);
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

    public String toString2(Tree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append(rule.ref);
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
            for (Iterator<Name> it = lookAhead.iterator(); it.hasNext(); ) {
                Name la = it.next();
                if (la.name.equals("$")) {
                    sb.append(la);
                }
                else {
                    TokenDecl decl = tree.getToken(la.name);
                    if (decl.rhs.isString()) {
                        sb.append(decl.rhs.asString().value);
                    }
                    else {
                        sb.append(la);
                    }
                }
                if (it.hasNext()) sb.append("/");
            }
        }
        return sb.toString();
    }

    //if dot follows a terminal
    public boolean isDotNonTerminal() {
        Name name = getDotSym();
        return name != null && !name.isToken;
    }

    public boolean isEpsilon() {
        return isEpsilon(rule);
    }

    //node after dot
    public Name getDotSym() {
        Node node = getDotNode();
        return node == null ? null : (node.isName() ? node.asName() : node.asRegex().node.asName());
    }

    //node after dot
    public Node getDotNode() {
        if (isEpsilon()) return null;
        Sequence rhs = rule.rhs.asSequence();
        if (dotPos < rhs.size()) {
            return rhs.get(dotPos);
        }
        return null;
    }

    //2 node after dot
    public Node getDotNode2() {
        Sequence rhs = rule.rhs.asSequence();
        if (dotPos < rhs.size() - 1) {
            return rhs.get(dotPos + 1);
        }
        return null;
    }

    public Name getDotSym2() {
        Node node = getDotNode2();
        return node == null ? null : (node.isName() ? node.asName() : node.asRegex().node.asName());
    }

    public Set<Name> follow(Tree tree) {
        HashSet<Name> res = new HashSet<>();
        Sequence rhs = rule.rhs.asSequence();
        boolean allEmpty = true;
        for (int i = dotPos + 1; i < rhs.size(); ) {
            Node node = rhs.get(i);
            res.addAll(FirstSet.tokens(node, tree));
            if (Helper.canBeEmpty(node, tree)) {
                i++;
                //look next node
            }
            else {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            //no end,la is carried
            res.addAll(lookAhead);
        }
        return res;
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
        if (hash == -1) {
            //lookahead may change later so consider initial set
            hash = Objects.hash(rule, dotPos, lookAhead);
        }
        return hash;
    }
}
