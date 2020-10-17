package gen;

import nodes.NameNode;
import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;
import utils.Helper;

import java.util.*;

public class Lr1ItemSet {
    List<Lr1Item> first;
    List<Lr1Item> all = new ArrayList<>();
    int curIndex = 0;//rule index
    Set<Lr0Item> done = new LinkedHashSet<>();
    Tree tree;

    public Lr1ItemSet(List<Lr1Item> first, Tree tree) {
        this.first = first;
        this.tree = tree;
        all.addAll(this.first);
    }

    public Lr1ItemSet(Lr1Item first, Tree tree) {
        this.first = new ArrayList<>(Collections.singletonList(first));
        this.tree = tree;
        all.addAll(this.first);
    }

    @Override
    public String toString() {
        //sort();
        return Helper.join(all, "\n");
    }

    //get first item that can transit
    public Lr1Item findTransitable() {
        for (int i = curIndex; i < all.size(); i++) {
            Lr1Item item = all.get(i);
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
        for (Lr1Item item : first) {
            if (item.isDotTerminal()) {
                closure(item.getDotNode());
            }
        }

    }

    void closure(NameNode node) {
        if (!node.isToken) {
            List<RuleDecl> ruleDecl = tree.getRules(node.name);
            for (RuleDecl decl : ruleDecl) {
                Lr1Item item = new Lr1Item(decl, 0);
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



}
