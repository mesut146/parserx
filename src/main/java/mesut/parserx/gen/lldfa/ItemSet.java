package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.gen.lr.TreeInfo;
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
    public int stateId;
    public static int lastId = 0;
    public LrType type;
    Tree tree;
    TreeInfo treeInfo;
    public List<LLTransition> transitions = new ArrayList<>();
    public List<LLTransition> incoming = new ArrayList<>();
    public Node symbol;
    boolean alreadyGenReduces = false;

    public ItemSet(TreeInfo treeInfo, LrType type) {
        this.treeInfo = treeInfo;
        this.type = type;
        this.tree = treeInfo.tree;
        stateId = lastId++;
    }

    public void addItem(Item item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
            item.itemSet = this;
        }
    }

    public void addAll(List<Item> list) {
        for (var it : list) {
            addItem(it);
        }
    }

    public void addTransition(LLTransition tr) {
        transitions.add(tr);
        tr.target.incoming.add(tr);
    }

    public void addTransition(Node sym, ItemSet target) {
        for (var t : transitions) {
            if (t.symbol.equals(sym) && t.target == target) return;
        }

        var tr = new LLTransition(this, target, sym);
        transitions.add(tr);
        target.incoming.add(tr);
    }

    public boolean hasFinal() {
        for (var it : all) {
            if (it.isFinalReduce(tree)) return true;
        }
        return false;
    }

    void gen(Item it, List<Item> list) {
        if (!it.isReduce(tree)) return;
        //is there any transition with my reduce symbol
        for (var gt : it.gotoSet) {
            for (var gti : gt.all) {
                for (int i = gti.dotPos; i < gti.rhs.size(); i++) {
                    if (i > gti.dotPos && !FirstSet.canBeEmpty(gti.getNode(i - 1), tree)) break;
                    var sym = sym(gti.getNode(i));
                    if (sym.equals(it.rule.ref)) {
                        int newPos = gti.getNode(i).isStar() ? i : i + 1;
                        var target = new Item(gti, newPos);
                        //target.advanced = gti.getNode(i).isStar();
                        if (target.isReduce(tree)) {
                            it.reduceParent.add(target);
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
        for (var it : kernel) {
            gen(it, res);
        }
        return res;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(all.get(i).toString2(tree));
            if (i < all.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public void closure() {
        if (!alreadyGenReduces) {
            addAll(genReduces());
        }
        alreadyGenReduces = true;
        for (Item item : kernel) {
            closure(item);
        }
        for (var it : all) {
            if (it.dotPos == 0) {
                it.gotoSet.add(this);
            }
        }
    }

    boolean common(Node s1, Node s2) {
        return new FactorHelper(tree, new Factor(tree)).common(s1, s2) != null;
    }

    public static Name sym(Node node) {
        return node.isName() ? node.asName() : node.asRegex().node.asName();
    }

    boolean isFactor(Item item, int i) {
        var syms = symbols();
        var node = item.getNode(i);
        var sym = sym(node);
        //check two consecutive syms have common
        for (int j = item.dotPos; j < item.rhs.size(); j++) {
            if (i == j) continue;
            if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
            var next = item.getNode(j);
            if (common(node, next)) {
                return true;
            }
        }
        //check dot sym and any other sym have common
        for (var s2 : syms) {
            if (s2 == sym) continue;
            if (common(sym, s2)) {
                return true;
            }
        }//for
        return false;
    }

    List<Name> symbols() {
        List<Name> res = new ArrayList<>();
        for (var item : all) {
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                var node = item.getNode(i);
                res.add(sym(node));
            }
        }
        return res;
    }

    public void closure(Item item) {
        //if dot sym have common factor ,closure is forced to reveal factor
        var syms = symbols();
        for (int i = item.dotPos; i < item.rhs.size(); i++) {
            if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
            var node = item.getNode(i);
            var sym = sym(node);
            if (sym.isToken) continue;
            //check two consecutive syms have common
            for (int j = item.dotPos; j < item.rhs.size(); j++) {
                if (i == j) continue;
                if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                var next = item.getNode(j);
                if (common(node, next)) {
                    item.closured[i] = true;
                    closure(sym, i, item);
                    break;
                }
            }
            if (item.closured[i]) continue;
            //check dot sym and any other sym have common
            for (var s2 : syms) {
                if (s2 == sym) continue;
                if (common(sym, s2)) {
                    item.closured[i] = true;
                    closure(sym, i, item);
                    break;
                }
            }
        }
    }

    private void closure(Name sym, int pos, Item sender) {
        Set<Name> laList = sender.follow(tree, pos);
        Set<Item> set = new HashSet<>();
        for (RuleDecl decl : treeInfo.ruleMap.get(sym.name)) {
            Item newItem = new Item(decl, 0);
            newItem.lookAhead.addAll(laList);
            newItem.parents.add(sender);
            addOrUpdate(newItem);
            set.add(newItem);
        }
        for (Item item : set) {
            item.siblings.addAll(set);
        }
    }

    void addOrUpdate(Item item) {
        if (!update(item, true, false)) {
            all.add(item);
            item.itemSet = this;
            closure(item);
        }
    }

    public boolean update(Item item, boolean updateIds, boolean updateGoto) {
        for (Item prev : all) {
            if (!prev.isSame(item)) continue;
            //merge la
            prev.lookAhead.addAll(item.lookAhead);
            if (updateIds) {
                prev.ids.addAll(item.ids);
//                if (prev.dotPos){
//                    for (Item sender : prev.senders) {
//                        sender.ids.addAll(item.ids);
//                    }
//                }
            }
            prev.parents.addAll(item.parents);
            prev.prev.addAll(item.prev);
            prev.gotoSet.addAll(item.gotoSet);
            updateChildren(prev);
            return true;
        }
        return false;
    }

    private void updateChildren(Item prev) {
        /*for (Item ch : all) {
            if (!ch.senders.contains(prev)) continue;
            ch.lookAhead.add()
        }*/
    }


    @Override
    public int hashCode() {
        return stateId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj.getClass() != ItemSet.class) return false;
        ItemSet other = (ItemSet) obj;
        return stateId == other.stateId;
    }
}