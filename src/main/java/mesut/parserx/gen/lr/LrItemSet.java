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
    private final List<LrTransition> incoming = new ArrayList<>();
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

    public void addItem(LrItem item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
        }
    }

    public void addAll(List<LrItem> list) {
        for (var it : list) {
            addItem(it);
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
        target.incoming.add(tr);
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
        for (LrItem item : kernel) {
            closure(item);
        }
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
        if (node.isToken) {
            throw new RuntimeException("closure error on node: " + node + ", was expecting rule");
        }
        Set<Name> laList = sender.follow(treeInfo.tree, pos);
        for (var rd : treeInfo.ruleMap.get(node)) {
            LrItem newItem = new LrItem(rd, 0);
            newItem.lookAhead = new HashSet<>(laList);
            newItem.parent = sender;
            addOrUpdate(newItem);
        }
    }

    void addOrUpdate(LrItem item) {
        if (!update(item)) {
            all.add(item);
            //todo la check
            closure(item);
        }
    }

    boolean update(LrItem item) {
        var prev = all.stream().filter(prv -> prv.isSame(item)).findFirst();
        if (prev.isEmpty()) {
            return false;
        }
        //merge la
        var prevItem = prev.get();
        //todo next items
        //update closure items too
        var la = prevItem.lookAhead;
        la.addAll(item.lookAhead);
        updateLa(prevItem, la, new HashSet<>());
        return true;
    }

    LrItem findChild(LrItem item) {
        for (var ch : all) {
            if (ch.parent == item) {
                return ch;
            }
        }
        return null;
    }

    void updateLa(LrItem item, Set<Name> la, Set<LrItem> done) {
        if (done.contains(item)) return;
        done.add(item);
        item.lookAhead = la;
        if (item.parent != null) {
            //updateLa(item.parent, la, done);
        }
        var ch = findChild(item);
        if (ch != null) {
            updateLa(ch, la, done);
        }
        for (var next : item.next) {
            updateLa(next, la, done);
        }
        //todo transitions
    }
}
