package mesut.parserx.gen.lr;

import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LrItemSet {
    public Set<LrItem> kernel = new HashSet<>();
    public List<LrItem> all = new ArrayList<>();
    public int stateId;
    public LrType type;
    public List<LrTransition> transitions = new ArrayList<>();
    TreeInfo treeInfo;

    public LrItemSet(TreeInfo tree, LrType type) {
        this.treeInfo = tree;
        this.type = type;
    }

    public void addCore(LrItem item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
            item.set = this;
        }
    }

    public void setCore(List<LrItem> list) {
        for (var it : list) {
            addCore(it);
        }
    }

    public void addTransition(LrTransition tr) {
        transitions.add(tr);
    }

    public void addTransition(Name sym, LrItemSet target) {
        for (var t : transitions) {
            if (t.symbol.equals(sym) && t.target == target) return;
        }

        var tr = new LrTransition(this, target, sym);
        transitions.add(tr);
    }

    public boolean hasReduce() {
        return !getReduce().isEmpty();
    }

    public List<LrItem> getReduce() {
        return all.stream().filter(i -> i.isReduce(treeInfo.tree)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(all.get(i).toString2(treeInfo.tree));
            if (i < all.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public void closure() {
        //System.out.println("closure " + stateId);
        for (var item : kernel) {
            closure(item);
        }
        computeLa();
    }

    //computing la alltogether is better bc all items are present
    void computeLa() {
        //rerun loop if any change is made,bc any change could affect child la
        boolean modified = true;
        while (modified) {
            modified = false;
            for (int i = kernel.size(); i < all.size(); i++) {
                var item = all.get(i);
                Set<Name> la = new HashSet<>();
                //find parent items and get follow sets
                all.stream()
                        .filter(pr -> isParent(pr, item))
                        .forEach(pr -> la.addAll(pr.follow(treeInfo.tree, pr.dotPos)));
                if (item.lookAhead.size() != la.size()) {
                    modified = true;
                }
                item.lookAhead.addAll(la);
            }
        }
    }

    boolean isParent(LrItem parent, LrItem ch) {
        return !parent.isReduce(treeInfo.tree) && ItemSet.sym(parent.getNode(parent.dotPos)).equals(ch.rule.ref);
    }

    public void closure(LrItem item) {
        if (item.dotPos == item.rhs.size()) return;
        Node node = item.getNode(item.dotPos);
        Name sym = ItemSet.sym(node);
        if (sym.isRule()) {
            closure(sym, item.dotPos, item);
        }
//        for (int i = item.dotPos; i < item.rhs.size(); i++) {
//            if (i > item.dotPos && !FirstSet.canBeEmpty(item.getNode(i - 1), treeInfo.tree)) break;
//            Node node = item.getNode(i);
//            Name sym = ItemSet.sym(node);
//            if (sym.isRule()) {
//                closure(sym, i, item);
//            }
//        }
    }

    private void closure(Name node, int pos, LrItem sender) {
        Set<Name> laList = sender.follow(treeInfo.tree, pos);
        List<LrItem> news = new ArrayList<>();
        for (var rd : treeInfo.ruleMap.get(node)) {
            var newItem = new LrItem(rd, 0);
            newItem.lookAhead.addAll(laList);
            news.add(newItem);
            //addOrUpdate(newItem);
        }
        //filter added
        var closured = new ArrayList<LrItem>();
        for (var item : news) {
            if (!has(item)) {
                all.add(item);
                item.set = this;
                closured.add(item);
            }
        }
        closured.forEach(this::closure);
    }

    boolean has(LrItem item) {
        return all.stream().anyMatch(prv -> prv.isSame(item));
    }

    //true if has and updated
    boolean update(LrItem item) {
        var old = all.stream().filter(prv -> prv.isSame(item)).findFirst();
        if (old.isEmpty()) {
            return false;
        }
        //merge la
        var oldItem = old.get();
        oldItem.lookAhead.addAll(item.lookAhead);
        updateChildren(oldItem, new HashSet<>());
        return true;
    }

    //update closure items(children)
    void updateChildren(LrItem parent, Set<Integer> done) {
        if (done.contains(parent.id)) return;
        done.add(parent.id);
        if (!parent.isReduce(treeInfo.tree)) {
            var chNode = parent.getNode(parent.dotPos);
            Set<Name> newLa = null;
            for (var ch : all) {
                if (ch.isReduce(treeInfo.tree)) continue;
                if (!chNode.equals(ch.rule.ref)) continue;
                if (newLa == null) {
                    newLa = parent.follow(treeInfo.tree, parent.dotPos);
                }
                ch.lookAhead.addAll(newLa);
                //done.remove(ch.id);
                updateChildren(ch, done);
            }
        }
        //update next
        for (var cur : parent.next) {
            cur.lookAhead.addAll(parent.lookAhead);
            cur.set.updateChildren(cur, new HashSet<>());
        }
    }

}
