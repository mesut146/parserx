package gen;

import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;
import utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lr0ItemSet {
    Lr0Item first;
    List<Lr0Item> others = new ArrayList<>();
    List<Lr0Item> all = new ArrayList<>();
    int curIndex = 0;
    Tree tree;

    public Lr0ItemSet(Lr0Item first, Tree tree) {
        this.first = first;
        this.tree = tree;
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
        if (first.isDotTerminal()) {
            closure(first.getDotNode());
        }
        all.add(first);
        all.addAll(others);
    }

    void closure(Node node) {
        if (node.isName() && !node.asName().isToken) {
            RuleDecl ruleDecl = tree.getRule(node.asName().name);
            Lr0Item item = new Lr0Item(ruleDecl, 0);
            others.add(item);
            if (item.isDotTerminal()) {
                closure(item.getDotNode());
            }
        }
        else {
            throw new RuntimeException("closure error on non name node");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(first);
        sb.append("\n");
        sb.append(Helper.join(others, "\n"));
        return sb.toString();
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
}
