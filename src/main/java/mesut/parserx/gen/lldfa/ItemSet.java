package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemSet {
    public Set<Item> kernel = new HashSet<>();
    public List<Item> all = new ArrayList<>();
    public boolean isStart = false;
    public int stateId = -1;
    public static int lastId = 0;
    public String type;
    Tree tree;
    public List<Transition> transitions = new ArrayList<>();
    public List<Transition> incomings = new ArrayList<>();
    public Node symbol;

    public ItemSet(Tree tree, String type) {
        this.tree = tree;
        this.type = type;
        stateId = lastId++;
    }

    public void addItem(Item item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
        }
    }

    public void addAll(List<Item> list) {
        for (Item it : list) {
            addItem(it);
        }
    }

    public void addTransition(Node sym, ItemSet target) {
        for (Transition t : transitions) {
            if (t.symbol.equals(sym) && t.target == target) return;
        }

        Transition tr = new Transition(this, sym, target);
        transitions.add(tr);
        target.incomings.add(tr);
    }

    public void addComing(Node sym, ItemSet from) {
        incomings.add(new Transition(from, sym, this));
    }

    public boolean hasReduce() {
        return !getReduce().isEmpty();
    }

    public List<Item> getReduce() {
        List<Item> list = new ArrayList<>();
        for (Item item : all) {
            if (item.hasReduce()) {
                list.add(item);
            }
        }
        return list;
    }

    void gen(Item it, List<Item> list) {
        if (!it.isReduce(tree)) return;
        //is there any transition with my reduce symbol
        for (ItemSet gt : it.gotoSet2) {
            for (Item gti : gt.all) {
                for (int i = gti.dotPos; i < gti.rhs.size(); i++) {
                    if (i > gti.dotPos && !FirstSet.canBeEmpty(gti.getNode(i - 1), tree)) break;
                    Name sym = sym(gti.getNode(i));
                    if (sym.equals(it.rule.ref)) {
                        int newPos = gti.getNode(i).isStar() ? i : i + 1;
                        Item target = new Item(gti, newPos);
                        target.advanced = gti.getNode(i).isStar();
                        if (target.isReduce(tree)) {
                            it.reduceParent.add(target);
                            target.reduceChild = it;
                        }
                        //target.gotoSet2.add(gt);
                        if (!list.contains(target)) {
                            list.add(target);
                            gen(target, list);
                        }
                    }
                }
            }
        }
    }

    public List<Item> genReduces() {
        List<Item> res = new ArrayList<>();
        for (Item it : kernel) {
            gen(it, res);
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(all.get(i).toString2(tree));
            if (i < all.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public void closure() {
        addAll(genReduces());
        for (Item item : kernel) {
            closure(item);
        }
        for (Item it : all) {
            if (it.dotPos == 0) {
                it.gotoSet2.add(this);
            }
        }
    }

    boolean common(Node s1, Node s2) {
        return new FactorHelper(tree, new Factor(tree)).common(s1, s2) != null;
    }

    Name sym(Node node) {
        return node.isName() ? node.asName() : node.asRegex().node.asName();
    }

    public void closure(Item item) {
        //if dot sym have common factor ,closure is forced to reveal factor
        List<Name> syms = symbols();
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
            Node node = item.getNode(i);
            Name sym = sym(node);
            if (sym.isToken) continue;
            //check two consecutive syms have common
            for (int j = item.dotPos; j < item.rhs.size(); j++) {
                if (i == j) continue;
                if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                Node next = item.getNode(j);
                if (common(node, next)) {
                    item.closured[i] = true;
                    closure(sym, item);
                    break;
                }
            }
            if (item.closured[i]) continue;
            //check dot sym and any other sym have common
            for (Name s2 : syms) {
                if (s2 == sym) continue;
                if (common(sym, s2)) {
                    item.closured[i] = true;
                    closure(sym, item);
                    break;
                }
            }//for
        }
    }

    boolean isFactor(Item item, int i) {
        List<Name> syms = symbols();
        Node node = item.getNode(i);
        Name sym = sym(node);
        //check two consecutive syms have common
        for (int j = item.dotPos; j < item.rhs.size(); j++) {
            if (i == j) continue;
            if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
            Node next = item.getNode(j);
            if (common(node, next)) {
                return true;
            }
        }
        //check dot sym and any other sym have common
        for (Name s2 : syms) {
            if (s2 == sym) continue;
            if (common(sym, s2)) {
                return true;
            }
        }//for
        return false;
    }

    List<Name> symbols() {
        List<Name> res = new ArrayList<>();
        for (Item item : all) {
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                Node node = item.getNode(i);
                res.add(sym(node));
            }
        }
        return res;
    }

    private void closure(Name node, Item sender) {
        if (node.isToken) return;

        Set<Name> laList = sender.follow(tree);
        Set<Item> set = new HashSet<>();
        for (RuleDecl decl : tree.getRules(node)) {
            Item newItem = new Item(decl, 0);
            if (!type.equals("lr0")) {
                newItem.lookAhead = new HashSet<>(laList);
            }
            newItem.senders.add(sender);
            addOrUpdate(newItem);
            set.add(newItem);
        }
        for (Item item : set) {
            item.siblings.addAll(set);
            item.siblings.remove(item);
        }
    }

    void addOrUpdate(Item item) {
        if (type.equals("lr0")) return;
        if (!update(item, false)) {
            all.add(item);
            closure(item);
        }
    }

    public boolean update(Item item, boolean updateIds) {
        for (Item prev : all) {
            if (!prev.isSame(item)) continue;
            //merge la
            prev.lookAhead.addAll(item.lookAhead);
            if (updateIds) prev.ids.addAll(item.ids);
            prev.senders.addAll(item.senders);
            return true;
        }
        return false;
    }


}