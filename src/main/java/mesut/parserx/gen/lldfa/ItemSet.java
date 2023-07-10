package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.lr.TreeInfo;
import mesut.parserx.gen.transform.FactorHelper;
import mesut.parserx.nodes.Factored;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class ItemSet {
    public Set<Item> kernel = new HashSet<>();
    public List<Item> all = new ArrayList<>();
    public boolean isStart = false;
    public int stateId;
    public static int lastId = 0;
    Tree tree;
    TreeInfo treeInfo;
    public List<LLTransition> transitions = new ArrayList<>();
    public List<LLTransition> incoming = new ArrayList<>();
    boolean alreadyGenReduces = false;
    boolean noClosure = false;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<Integer> which = Optional.empty();
    public static boolean forceClosure = false;

    public ItemSet(TreeInfo treeInfo) {
        this.treeInfo = treeInfo;
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
        for (var old : transitions) {
            if (old.symbol.equals(tr.symbol) && old.target == tr.target) return;
        }
        transitions.add(tr);
        tr.target.incoming.add(tr);
    }

    public void addTransition(Node sym, ItemSet target) {
        addTransition(new LLTransition(this, target, sym));
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
                    if (gti.getNode(i) instanceof Factored) continue;
                    var sym = sym(gti.getNode(i));
                    if (!sym.equals(it.rule.ref)) continue;
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
            sb.append(all.get(i).toString());
            if (i < all.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public void closure() {
        if (noClosure) return;
        if (!alreadyGenReduces) {
            addAll(genReduces());
        }
        alreadyGenReduces = true;
        for (var item : kernel) {
            closure(item);
        }
        for (var it : all) {
            if (it.dotPos == 0) {
                it.gotoSet.add(this);
            }
        }
    }

    boolean common(Name s1, Name s2) {
        return FactorHelper.hasCommon(s1, s2, tree);
    }

    boolean commonNoSame(Name s1, Name s2) {
        if (s1.equals(s2)) return false;
        return FactorHelper.hasCommon(s1, s2, tree);
    }

    public static Name sym(Node node) {
        return node.isName() ? node.asName() : node.asRegex().node.asName();
    }

    boolean isFactor(Item item, int i, boolean checkConsecutive) {
        var node = item.getNode(i);
        if (node instanceof Factored) return false;
        var sym = sym(node);
        if (checkConsecutive) {
            //check two consecutive syms have common
            for (int j = item.dotPos; j < item.rhs.size(); j++) {
                if (i == j) continue;
                if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                var next = item.getNode(j);
                if (next instanceof Factored) continue;
                if (common(sym, sym(next))) {
                    return true;
                }
            }
        }
        //check dot sym and any other sym have common
        for (var it : all) {
            if (it == item) continue;
            for (var s : it.getSyms(tree)) {
                if (s.getKey() instanceof Factored) continue;
                //todo if closured item is factor, child item incorrectly becomes non-factor
                if (sym(s.getKey()).equals(sym)) {
                    return true;
                }
            }
        }
        return false;
    }

    List<Name> symbols() {
        var res = new ArrayList<Name>();
        for (var item : all) {
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                var node = item.getNode(i);
                if (node instanceof Factored) continue;
                res.add(sym(node));
            }
        }
        return res;
    }

    public void closure(Item item) {
        if (forceClosure) {
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                var node = item.getNode(i);
                if (node instanceof Factored) continue;
                var sym = sym(node);
                if (sym.isToken) continue;
                item.closured[i] = true;
                closure(sym, i, item);
            }
        }
        else {
            //if dot sym have common factor ,closure is forced to reveal factor
            var syms = symbols();
            for (int i = item.dotPos; i < item.rhs.size(); i++) {
                if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), tree)) break;
                var node = item.getNode(i);
                if (node instanceof Factored) continue;
                var sym = sym(node);
                if (sym.isToken) continue;
                //check two consecutive syms have common
                for (int j = item.dotPos; j < item.rhs.size(); j++) {
                    if (i == j) continue;
                    if (j > item.dotPos && !FirstSet.canBeEmpty(item.getNode(j - 1), tree)) break;
                    var next = item.getNode(j);
                    if (commonNoSame(sym, sym(next))) {
                        item.closured[i] = true;
                        closure(sym, i, item);
                        break;
                    }
                }
                if (item.closured[i]) continue;
                //check dot sym and any other sym have common
                for (var s2 : syms) {
                    if (s2 == sym) continue;
                    if (commonNoSame(sym, s2)) {
                        item.closured[i] = true;
                        closure(sym, i, item);
                        break;
                    }
                }
            }
        }

    }

    private void closure(Name sym, int pos, Item sender) {
        var laList = sender.follow(tree, pos);
        for (var decl : treeInfo.ruleMap.get(sym)) {
            var newItem = new Item(decl, 0);
            if (sender.firstParents.isEmpty()) {
                newItem.firstParents.add(sender);
            }
            else {
                newItem.firstParents = sender.firstParents;
            }
            newItem.lookAhead.addAll(laList);
            newItem.parents.add(sender);
            addOrUpdate(newItem);
        }
    }

    void addOrUpdate(Item item) {
        if (!update(item)) {
            all.add(item);
            item.itemSet = this;
            closure(item);
        }
    }

    public boolean update(Item item) {
        for (var prev : all) {
            if (!prev.isSame(item)) continue;
            prev.lookAhead.addAll(item.lookAhead);
            prev.firstParents.addAll(item.firstParents);
            prev.ids.addAll(item.ids);
            prev.parents.addAll(item.parents);
            prev.prev.addAll(item.prev);
            prev.gotoSet.addAll(item.gotoSet);
            prev.getSyms(tree).forEach(e -> e.getKey().astInfo.isFactor = true);
            return true;
        }
        return false;
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