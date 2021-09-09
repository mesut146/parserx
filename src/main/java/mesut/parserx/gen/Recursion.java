package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class Recursion {
    public static boolean debug = false;
    Tree tree;

    public Recursion(Tree tree) {
        this.tree = tree;
    }

    public void all() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (Helper.first(decl.rhs, tree, true).contains(decl.ref())) {
                if (debug) {
                    System.out.println("removing recursion on " + decl);
                }
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
        //info.zero.astInfo.outerVar = "res";
        //info.one.astInfo.outerVar = "res";
        decl.rhs = Sequence.of(info.zero, new Regex(info.one, "*"));
        decl.isRecursive = true;
    }

}
