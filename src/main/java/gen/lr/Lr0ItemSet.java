package gen.lr;

import nodes.NameNode;
import nodes.NodeList;
import nodes.RuleDecl;
import nodes.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lr0ItemSet<T extends LrItem> {
    List<T> kernel = new ArrayList<>();
    List<T> all = new ArrayList<>();
    Tree tree;

    public Lr0ItemSet() {
    }

    public Lr0ItemSet(List<T> kernel, Tree tree) {
        this.kernel = kernel;
        this.tree = tree;
    }

    public Lr0ItemSet(T kernel, Tree tree) {
        this(new ArrayList<>(Collections.singletonList(kernel)), tree);
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

    public void closure(NameNode node, LrItem it) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                T item = (T) new LrItem(decl, 0);
                if (!all.contains(item)) {
                    all.add(item);
                    if (kernel.contains(item)) {
                        //todo
                    }
                    if (item.isDotNonTerminal()) {
                        closure(item.getDotNode(), item);
                    }
                }
            }

        }
        else {
            throw new RuntimeException("closure error on node: " + node);
        }
    }

    @Override
    public String toString() {
        //sort();
        return NodeList.join(all, "\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lr0ItemSet itemSet = (Lr0ItemSet) o;
        return Objects.equals(kernel, itemSet.kernel);
    }

    @Override
    public int hashCode() {
        return kernel.hashCode();
    }

}
