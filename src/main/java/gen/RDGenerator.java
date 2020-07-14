package gen;

import nodes.Tree;
import rule.RuleDecl;

public class RDGenerator {
    String dir;
    Tree tree;

    public RDGenerator(Tree tree) {
        this.tree = tree;
    }

    public void generate() {
        PrepareTree.checkReferences(tree);
        for (RuleDecl decl : tree.rules) {
            isRule(decl);
        }
    }

    void isRule(RuleDecl decl) {

    }
}
