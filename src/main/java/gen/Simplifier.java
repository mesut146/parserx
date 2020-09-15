package gen;

import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;

public class Simplifier {
    Tree tree;

    public Simplifier(Tree tree) {
        this.tree = tree;
    }

    public void simplify() {
        for (RuleDecl decl : tree.rules) {
            Node rhs = decl.rhs;
            if (rhs.isName()) {
                //remove this and replace all references to this
            }
        }
    }
}
