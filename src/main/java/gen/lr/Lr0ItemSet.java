package gen.lr;

import nodes.NameNode;
import nodes.NodeList;
import nodes.RuleDecl;
import nodes.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lr0ItemSet extends LrItemSet {

    public Lr0ItemSet() {
    }

    public Lr0ItemSet(List<LrItem> kernel, Tree tree) {
        super(kernel, tree);
    }

    public Lr0ItemSet(LrItem kernel, Tree tree) {
        super(kernel, tree);
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
                LrItem item = new LrItem(decl, 0);
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

}
