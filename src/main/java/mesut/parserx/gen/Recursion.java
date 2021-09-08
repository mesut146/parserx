package mesut.parserx.gen;

import mesut.parserx.nodes.Regex;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Sequence;
import mesut.parserx.nodes.Tree;

public class Recursion {
    Tree tree;

    public Recursion(Tree tree) {
        this.tree = tree;
    }

    public void all() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (Helper.first(decl.rhs, tree, true).contains(decl.ref())) {
                handle(decl);
            }
        }
    }

    public void handle(RuleDecl decl) {
        //A= A A(A) | A_no_A
        //A= A_no_A A(A)*
        Factor.PullInfo info = new Factor(tree).pullRule(decl.ref(), decl.ref());
        decl.rhs = Sequence.of(info.zero, new Regex(info.one, "*"));
        //TOdo ast
    }

}
