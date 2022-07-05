package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

public class Item {
    public RuleDecl rule;
    public int dotPos;
    public Set<Name> lookAhead = new TreeSet<>();
    public Set<Integer> ids = new TreeSet<>();
    public Sequence rhs;
    public Set<ItemSet> gotoSet = new HashSet<>();
    public boolean[] closured;
    public List<Item> senders = new ArrayList<>();//prev item
    public List<Item> reduceParent = new ArrayList<>();
    public Set<Item> siblings = new HashSet<>();
    public Item reduceChild;
    public boolean advanced = false;//dot star but advanced
    public ItemSet itemSet;
    public static int lastId = 0;

    public Item(RuleDecl rule, int dotPos) {
        this.rule = rule;
        this.dotPos = dotPos;
        this.rhs = rule.rhs.asSequence();
        if (isEpsilon()) {
            //act as reduce
            this.dotPos = 1;
        }
        ids.add(lastId++);
        closured = new boolean[rhs.size()];
    }

    public Item(Item item, int dotPos) {
        this(item.rule, dotPos);
        this.lookAhead.addAll(item.lookAhead);
        this.gotoSet = item.gotoSet;
        this.ids = new HashSet<>(item.ids);
        this.senders.add(item);
        this.siblings = item.siblings;
        if (item.getNode(item.dotPos).isStar()) {
            advanced = true;
        }
        lastId--;
    }

    public boolean isAlt() {
        return rule.which != -1;
    }

    public boolean isEpsilon() {
        if (rhs.size() == 1) return rhs.get(0).isEpsilon();
        return false;
    }

    //dot end or rest is empty
    public boolean isReduce(Tree tree) {
        for (int i = dotPos; i < rhs.size(); i++) {
            Node node = rhs.get(i);
            if (!Helper.canBeEmpty(node, tree)) return false;
        }
        return true;
    }

    boolean isLr0() {
        return lookAhead.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rule.ref);
        if (rule.which != -1) {
            sb.append("#").append(rule.which);
        }
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
        if (!isLr0()) {
            sb.append(" , ");
            sb.append(NodeList.join(new ArrayList<>(lookAhead), "/"));
        }
        sb.append(" ").append(ids);
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
        sb.append(" ").append(ids);
        return sb.toString();
    }


    public Node getNode(int pos) {
        Sequence s = rule.rhs.asSequence();
        if (pos < s.size())
            return s.get(pos);
        return null;
    }

    //first set of follow of pos node
    public Set<Name> follow(Tree tree, int pos) {
        HashSet<Name> res = new HashSet<>();
        if (getNode(pos).isStar()) {
            res.addAll(FirstSet.tokens(getNode(pos), tree));
        }
        boolean allEmpty = true;
        for (int i = pos + 1; i < rhs.size(); ) {
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

        Item item = (Item) other;

        if (dotPos != item.dotPos) return false;
        return Objects.equals(rule, item.rule) && lookAhead.equals(item.lookAhead);
    }

    //without lookahead
    public boolean isSame(Item other) {
        return dotPos == other.dotPos && Objects.equals(rule, other.rule);
    }

    @Override
    public int hashCode() {
        //lookahead may change later so consider initial set
        return Objects.hash(rule, dotPos, lookAhead);
    }
}
