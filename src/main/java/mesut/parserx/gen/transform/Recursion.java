package mesut.parserx.gen.transform;

import mesut.parserx.gen.Copier;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.*;

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
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i++);
            if (FirstSet.start(decl.rhs, decl.ref, tree)) {
                any = true;
                if (debug) {
                    System.out.println("removing recursion on " + decl.ref);
                }
                handle(decl, true);
            }
        }
    }

    public void handle(RuleDecl decl, boolean cycle) {
        List<Name> set = new ArrayList<>(FirstSet.firstSetNoRec(decl.rhs, tree));
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            if (it.next().isToken) {
                it.remove();
            }
        }
        Tree backup = Copier.copyTree(tree);

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

        if (!cycle) return;
        //solve other refs as well to get rif of greedy loops
        Collections.sort(set, new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                if (FirstSet.firstSetNoRec(tree.getRule(o1).rhs, tree).contains(o1)) {
                    //direct recursion has higher priority
                    return -1;
                }
                if (FirstSet.firstSetNoRec(tree.getRule(o2).rhs, tree).contains(o2)) {
                    return 1;
                }
                return 0;
            }
        });
        for (Name other : set) {
            if (other.isToken || other.equals(decl.ref)) continue;
            Tree newTree = Copier.copyTree(backup);
            Recursion rec = new Recursion(newTree);
            rec.handle(newTree.getRule(other), false);
            //add new rules
            tree.getRule(other).rhs = newTree.getRule(other).rhs;
            for (RuleDecl ruleDecl : rec.factor.declSet) {
                tree.addRule(ruleDecl);
            }
        }
    }


}
