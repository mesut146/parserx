package mesut.parserx.gen.lr;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.lldfa.Item;
import mesut.parserx.gen.lldfa.ItemSet;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RuleDecl;

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
    TreeInfo treeInfo;
    public List<LrTransition> transitions = new ArrayList<>();
    private List<LrTransition> incoming = new ArrayList<>();

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
        for (var rd : treeInfo.ruleMap.get(node.name)) {
            LrItem newItem = new LrItem(rd, 0);
            newItem.lookAhead = new HashSet<>(laList);
            newItem.sender = sender;
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
//        for (var entry : item.getSyms(treeInfo.tree)) {
//            if (entry.getKey().isName() && entry.getKey().asName().isToken) {
//                return false;
//            }
//        }
        var prev = all.stream().filter(prv -> prv.isSame(item)).findFirst();
        if (prev.isPresent()) {
            //merge la
            var prevItem = prev.get();
            prevItem.lookAhead.addAll(item.lookAhead);
            //update other items too
            for (LrItem cl : all) {
                if (cl.sender == prevItem) {
                    if (true) {
                        throw new RuntimeException("sender logic changed");
                    }
                    cl.lookAhead.addAll(prevItem.lookAhead);
                    update(cl);
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
}
