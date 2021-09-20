package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;

import java.util.*;

public abstract class LrItemSet {
    public Set<LrItem> kernel = new HashSet<>();
    public List<LrItem> all = new ArrayList<>();
    public Set<LrItem> done = new HashSet<>();
    public int stateId = -1;
    Tree tree;

    public LrItemSet(List<LrItem> kernel, Tree tree) {
        this.kernel = new HashSet<>(kernel);
        this.tree = tree;
    }

    public LrItemSet(LrItem kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
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

    public void closure() {
        if (all.isEmpty()) {
            all.addAll(kernel);
            for (LrItem item : kernel) {
                if (item.isDotNonTerminal()) {
                    closure(item.getDotNode(), item);
                }
            }
        }
    }

    public void closure(LrItem it) {
        if (it.isDotNonTerminal()) {
            closure(it.getDotNode(), it);
        }
    }

    public abstract void closure(Name node, LrItem sender);

    @Override
    public String toString() {
        return NodeList.join(all, "\n");
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        LrItemSet itemSet = (LrItemSet) o;
//        return Objects.equals(kernel, itemSet.kernel);
//    }
//
//    @Override
//    public int hashCode() {
//        return kernel.hashCode();
//    }
}
