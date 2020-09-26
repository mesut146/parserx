package gen;

import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;
import utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class Lr0ItemSet {
    Lr0Item first;
    List<Lr0Item> others = new ArrayList<>();
    Tree tree;

    public Lr0ItemSet(Lr0Item first) {
        this.first = first;
    }

    public void closure() {
        if (first.isDotTerminal()) {
            closure(first.getDotNode());
        }
    }

    void closure(Node node) {
        if (node.isName()) {
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
}
