package mesut.parserx.gen;

import mesut.parserx.nodes.*;

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
        Name sym = decl.ref();
        sym.astInfo.outerVar = "res";
        //sym.astInfo.isFactor = true;
        //sym.astInfo.factorName =;
        Factor.PullInfo info = new Factor(tree).pullRule(sym, sym);
        decl.rhs = Sequence.of(info.zero, new Regex(info.one, "*"));
        decl.isRecursive = true;
        //todo ast
    }

}
