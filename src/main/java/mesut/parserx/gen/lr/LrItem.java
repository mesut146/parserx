package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.*;

public class LrItem {
    public static int lastId = 0;
    public Set<Name> lookAhead = new HashSet<>();
    public RuleDecl rule;
    public Sequence rhs;
    public int dotPos;
    public Set<LrItemSet> gotoSet = new HashSet<>();
    public LrItem parent;
    public LrItem prev;
    public List<LrItem> next = new ArrayList<>();
    public Set<Integer> ids = new HashSet<>();
    int hash = -1;

    public LrItem(RuleDecl rule, int dotPos) {
        this.rule = rule;
        this.dotPos = dotPos;
        this.rhs = rule.rhs.asSequence();
        if (isEpsilon()) {
            //act as reduce
            this.dotPos = 1;
        }
        ids.add(lastId++);
    }

    public LrItem(LrItem item, int dotPos) {
        this(item.rule, dotPos);
        this.lookAhead = new HashSet<>(item.lookAhead);
        this.ids = new HashSet<>(item.ids);
        this.prev = item;
        item.next.add(this);
        lastId--;
    }

    public static boolean isEpsilon(RuleDecl decl) {
        Sequence rhs = decl.rhs.asSequence();
        if (rhs.size() == 1) {
            return rhs.get(0).isEpsilon();
        }
        return false;
    }

    public boolean isReduce(Tree tree) {
        return dotPos == rhs.size() || isEpsilon();
//        for (int i = dotPos; i < rhs.size(); i++) {
//            if (!Helper.canBeEmpty(rhs.get(i), tree)) return false;
//        }
//        return true;
    }

    @Override
    public String toString() {
        return toString2(null);
    }

    public String toString2(Tree tree) {
        var sb = new StringBuilder();
        sb.append(rule.ref);
        sb.append(": ");
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
        sb.append(" , ");
        if (tree == null) {
            sb.append(NodeList.join(new ArrayList<>(lookAhead), "/"));
        } else {
            for (var it = lookAhead.iterator(); it.hasNext(); ) {
                var la = it.next();
                if (la.name.equals("$")) {
                    sb.append(la);
                } else {
                    var decl = tree.getToken(la.name);
                    if (decl.rhs.isString()) {
                        sb.append(decl.rhs.asString().value);
                    } else if (decl.rhs.isSequence()) {
                        var seq = decl.rhs.asSequence();
                        if (seq.size() == 1 && seq.get(0).isString()) {
                            sb.append(seq.get(0).asString().value);
                        } else {
                            sb.append(la);
                        }
                    } else {
                        sb.append(la);
                    }
                }
                if (it.hasNext()) sb.append("/");
            }
        }
        return sb.toString();
    }

    public boolean isEpsilon() {
        return isEpsilon(rule);
    }

    public Node getNode(int pos) {
        var s = rule.rhs.asSequence();
        if (pos < s.size()) {
            return s.get(pos);
        }
        return null;
    }

    //usable symbols after dot
    public List<Map.Entry<Node, Integer>> getSyms(Tree tree) {
        List<Map.Entry<Node, Integer>> list = new ArrayList<>();
        for (int i = dotPos; i < rhs.size(); i++) {
            var node = getNode(i);
            list.add(new AbstractMap.SimpleEntry<>(node, i));
            if (!FirstSet.canBeEmpty(node, tree)) {
                break;
            }
        }
        return list;
    }

    //first set of follow of dot node
    public Set<Name> follow(Tree tree, int pos) {
        if (pos + 1 == rhs.size()) {
            //last pos
            return lookAhead;
        }
        var rest = new Sequence(rhs.list.subList(pos + 1, rhs.size()));
        var res = FirstSet.tokens(rest, tree);
        if (FirstSet.canBeEmpty(rest, tree)) {
            //no end,la is carried
            res.addAll(lookAhead);
        }
        return res;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        var item = (LrItem) other;
        return isSame(item) && lookAhead.equals(item.lookAhead);
    }

    //without lookahead
    public boolean isSame(LrItem other) {
        return dotPos == other.dotPos && Objects.equals(rule, other.rule);
    }

    public boolean isSameNoDot(LrItem other) {
        return Objects.equals(rule, other.rule) && rule.which.equals(other.rule.which);
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
