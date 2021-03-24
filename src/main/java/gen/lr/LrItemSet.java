package gen.lr;

import nodes.NameNode;
import nodes.NodeList;
import nodes.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LrItemSet {
    public List<LrItem> kernel = new ArrayList<>();
    public List<LrItem> all = new ArrayList<>();
    Tree tree;

    public LrItemSet() {
    }

    public LrItemSet(List<LrItem> kernel, Tree tree) {
        this.kernel = kernel;
        this.tree = tree;
    }

    public LrItemSet(LrItem kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
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
    }

    public void closure(LrItem it) {

    }

    public void closure(NameNode node, LrItem sender) {

    }

    @Override
    public String toString() {
        return NodeList.join(all, "\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LrItemSet itemSet = (LrItemSet) o;
        return Objects.equals(kernel, itemSet.kernel);
    }

    @Override
    public int hashCode() {
        return kernel.hashCode();
    }
}
