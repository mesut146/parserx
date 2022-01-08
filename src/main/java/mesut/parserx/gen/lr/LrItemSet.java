package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LrItemSet {
    public Set<LrItem> kernel = new HashSet<>();
    public List<LrItem> all = new ArrayList<>();
    public Set<LrItem> done = new HashSet<>();
    public int stateId = -1;
    public String type;
    Tree tree;


    public LrItemSet(Set<LrItem> kernels, Tree tree, String type) {
        this.kernel.addAll(kernels);
        this.tree = tree;
        this.type = type;
    }

    public void addCore(LrItem item) {
        if (!all.contains(item)) {
            kernel.add(item);
            all.add(item);
            closure(item);
        }
    }

    //get item that is not processed yet
    public LrItem getItem() {
        for (LrItem item : all) {
            if (!done.contains(item)) {
                done.add(item);
                return item;
            }
        }
        return null;
    }

    public boolean hasReduce() {
        return !getReduce().isEmpty();
    }

    public List<LrItem> getReduce() {
        List<LrItem> list = new ArrayList<>();
        for (LrItem item : all) {
            if (item.hasReduce()) {
                list.add(item);
            }
        }
        return list;
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
        if (all.isEmpty()) {
            all.addAll(kernel);
            for (LrItem item : kernel) {
                closure(item);
            }
        }
    }

    public void closure(LrItem it) {
        if (it.isDotNonTerminal()) {
            closure(it.getDotSym(), it);
        }
    }

    private void closure(Name node, LrItem sender) {
        if (node.isToken) {
            throw new RuntimeException("closure error on node: " + node + ", was expecting rule");
        }
        Set<Name> laList = sender.follow(tree);
        for (RuleDecl decl : tree.getRules(node)) {
            LrItem newItem = new LrItem(decl, 0);
            if (!type.equals("lr0")) {
                newItem.lookAhead = new HashSet<>(laList);
            }
            newItem.sender = sender;
            addItem(newItem);
        }
    }


    void addItem(LrItem item) {
        for (LrItem prev : all) {
            if (prev.isSame(item)) {
                if (type.equals("lr0")) {
                    return;
                }
                //merge la
                prev.lookAhead.addAll(item.lookAhead);
                //update other items too
                if (item.isDotNonTerminal()) {
                    for (LrItem cl : all) {
                        if (cl.sender == prev) {
                            cl.lookAhead.addAll(prev.lookAhead);
                            addItem(cl);
                        }
                    }
                }
                return;
            }
        }
        all.add(item);
        closure(item);
    }
}
