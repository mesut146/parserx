package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.Set;

//remove left recursion by factorization
public class Recursion {
    public static boolean debug = false;
    public boolean any;
    public Factor factor;
    Tree tree;

    public Recursion(Tree tree) {
        this.tree = tree;
        factor = new Factor(tree);
    }

    public void all() {
        /*LeftRecursive ll = new LeftRecursive(tree);
        ll.normalizeIndirects();*/

        //directOnes();

        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (FirstSet.start(decl.rhs, decl.ref, tree)) {
                any = true;
                if (debug) {
                    System.out.println("removing recursion on " + decl.ref);
                }
                handle(decl);
            }
        }
    }

    void directOnes() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (FirstSet.firstSetNoRec(decl.rhs, tree).contains(decl.ref)) {
                any = true;
                if (debug) {
                    System.out.println("removing direct recursion on " + decl.ref);
                }
                handle(decl);
            }
        }
    }

    public void handle(RuleDecl decl) {
        //A= A A(A) | A_no_A
        //A= A_no_A A(A)*
        Name sym = decl.ref.copy();
        //sym.astInfo.varName = "res";
        sym.astInfo.isFactor = true;
        sym.astInfo.factorName = "res";

        Name ref = decl.ref.copy();

        factor.curRule = decl;
        Factor.PullInfo info = factor.pullRule(ref, sym);
        info.zero.astInfo.varName = "res";
        info.zero.astInfo.isPrimary = true;
        info.one.astInfo.varName = "res";
        info.one.astInfo.isSecondary = true;
        decl.rhs = Sequence.make(info.zero, new Regex(info.one, "*"));
    }

    void bySeparateFactor(RuleDecl decl) {
        Set<Name> set = FirstSet.firstSet(decl.rhs, tree);
    }

}
