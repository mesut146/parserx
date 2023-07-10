package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.*;

public class Item {
    public RuleDecl rule;
    public Sequence rhs;
    public int dotPos;
    public Set<Name> lookAhead = new TreeSet<>();
    public Set<Integer> ids = new TreeSet<>();
    public Set<ItemSet> gotoSet = new HashSet<>();
    public boolean[] closured;
    public List<Item> parents = new ArrayList<>();//the ones created us
    public Set<Item> prev = new LinkedHashSet<>();//prev item
    public List<Item> reduceParent = new ArrayList<>();
    public ItemSet itemSet;
    public HashSet<Item> firstParents = new HashSet<>();
    public boolean first = false;
    public static boolean printLa = false;
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
        this.prev.add(item);
        this.first = item.first;
        this.firstParents = item.firstParents;
        lastId--;
    }

    public boolean isAlt() {
        return rule.which.isPresent();
    }

    public boolean isEpsilon() {
        if (rhs.size() == 1) return rhs.get(0).isEpsilon();
        return false;
    }

    //dot end or rest is empty
    public boolean isReduce(Tree tree) {
        for (int i = dotPos; i < rhs.size(); i++) {
            if (!FirstSet.canBeEmpty(rhs.get(i), tree)) return false;
        }
        return true;
    }

    public boolean isFinalReduce(Tree tree) {
        return first && isReduce(tree);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(rule.ref);
        rule.which.ifPresent(integer -> sb.append("#").append(integer));
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
        if (!lookAhead.isEmpty() && printLa) {
            sb.append(" , ");
            sb.append(NodeList.join(new ArrayList<>(lookAhead), "/"));
        }
        sb.append(" ").append(ids);
        return sb.toString();
    }

    public Node getNode(int pos) {
        var s = rule.rhs.asSequence();
        if (pos < s.size()) {
            return s.get(pos);
        }
        return null;
    }

    public List<Map.Entry<Node, Integer>> getSyms(Tree tree) {
        List<Map.Entry<Node, Integer>> list = new ArrayList<>();
        for (int i = dotPos; i < rhs.size(); i++) {
            var node = getNode(i);
            if (node instanceof Factored) continue;
            list.add(new AbstractMap.SimpleEntry<>(node, i));
            if (!FirstSet.canBeEmpty(node, tree)) {
                break;
            }
        }
        return list;
    }

    //first set of follow of pos node
    public Set<Name> follow(Tree tree, int pos) {
        var res = new HashSet<Name>();
        if (getNode(pos).isStar()) {
            res.addAll(FirstSet.tokens(getNode(pos), tree));
        }
        var allEmpty = true;
        for (int i = pos + 1; i < rhs.size(); ) {
            Node node = rhs.get(i);
            res.addAll(FirstSet.tokens(node, tree));
            if (FirstSet.canBeEmpty(node, tree)) {
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

        var item = (Item) other;
        return isSame(item) && lookAhead.equals(item.lookAhead);
    }

    //without lookahead
    public boolean isSame(Item other) {
        return dotPos == other.dotPos && rule.equals(other.rule);
    }

    @Override
    public int hashCode() {
        //lookahead may change later so consider initial set
        //return Objects.hash(rule, dotPos, lookAhead);
        return Objects.hash(rule, dotPos, ids);
    }
}
