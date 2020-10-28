package gen.lr;

import nodes.NameNode;
import nodes.Node;
import nodes.NodeList;
import nodes.Tree;
import nodes.RuleDecl;

import java.util.*;

public class Lr0ItemSet {
    List<Lr0Item> first;
    List<Lr0Item> all = new ArrayList<>();
    int curIndex = 0;//rule index
    Set<Lr0Item> done = new LinkedHashSet<>();
    Tree tree;

    public Lr0ItemSet(List<Lr0Item> first, Tree tree) {
        this.first = first;
        this.tree = tree;
        all.addAll(this.first);
    }

    public Lr0ItemSet(Lr0Item first, Tree tree) {
        this.first = new ArrayList<>(Collections.singletonList(first));
        this.tree = tree;
        all.addAll(this.first);
    }

    //get first item that can transit
    public Lr0Item findTransitable() {
        for (int i = curIndex; i < all.size(); i++) {
            Lr0Item item = all.get(i);
            if (!done.contains(item)) {
                Node token = item.getDotNode();
                if (token != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public void closure() {
        if (all.size() > 1) {
            return;
        }
        for (Lr0Item item : first) {
            if (item.isDotTerminal()) {
                closure(item.getDotNode());
            }
        }

    }

    void closure(NameNode node) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr0Item item = new Lr0Item(decl, 0);
                if (!all.contains(item)) {
                    all.add(item);
                    if (item.isDotTerminal()) {
                        closure(item.getDotNode());
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

        return Objects.equals(first, itemSet.first);
    }

    @Override
    public int hashCode() {
        return first.hashCode();
    }

}
