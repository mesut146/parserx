package mesut.parserx.gen.lr;

import mesut.parserx.nodes.NameNode;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.List;

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

    @Override
    public void closure(LrItem it) {

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
