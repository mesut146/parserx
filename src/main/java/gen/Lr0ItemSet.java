package gen;

import nodes.NameNode;
import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;
import utils.Helper;

import java.util.*;

public class Lr0ItemSet {
    Lr0Item first;
    List<Lr0Item> all = new ArrayList<>();
    int curIndex = 0;
    Tree tree;

    public Lr0ItemSet(Lr0Item first, Tree tree) {
        this.first = first;
        this.tree = tree;
        all.add(first);
    }

    int getIndex(Lr0Item item) {
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }

    //get first item that can transit
    public Lr0Item findTransitable() {
        for (int i = curIndex; i < all.size(); i++) {
            Lr0Item item = all.get(i);
            Node token = item.getDotNode();
            if (token != null) {
                return item;
            }
        }
        return null;
    }

    public void closure() {
        if (all.size() > 1) {
            return;
        }
        if (first.isDotTerminal()) {
            closure(first.getDotNode());
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
        return Helper.join(all, "\n");
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
        return first != null ? first.hashCode() : 0;
    }

    public void sort() {
        Collections.sort(all, new Comparator<Lr0Item>() {
            @Override
            public int compare(Lr0Item item, Lr0Item t1) {
                return item.ruleDecl.name.compareTo(t1.ruleDecl.name);
            }
        });
    }
}
