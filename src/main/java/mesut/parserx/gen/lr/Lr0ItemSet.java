package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;

import java.util.List;

public class Lr0ItemSet extends LrItemSet {

    public Lr0ItemSet(LrItem kernel, Tree tree) {
        super(kernel, tree);
    }

    public void closure(Name node, LrItem it) {
        if (node.isToken) {
            throw new RuntimeException("closure symbol is token: " + node + "in " + it);
        }
        List<RuleDecl> ruleDecl = tree.getRules(node);
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

}
