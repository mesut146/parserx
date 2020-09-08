package gen;

import nodes.Tree;
import rule.RuleDecl;

//remove left recursions
public class LeftRecursive {
    Tree tree;//must be bnf grammar

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public void transform() {
        for (RuleDecl decl : tree.rules) {
            transform(decl);
        }
    }

    private void transform(RuleDecl decl) {
        //handle direct
    }
}
