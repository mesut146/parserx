package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.nodes.*;

//remove left recursion by factorisation
public class Recursion {
    public static boolean debug = false;
    public boolean any;
    Tree tree;

    public Recursion(Tree tree) {
        this.tree = tree;
    }

    public void all() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (Helper.first(decl.rhs, tree, true).contains(decl.ref())) {
                any = true;
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
