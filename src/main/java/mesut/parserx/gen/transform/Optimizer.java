package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Iterator;

public class Optimizer extends Transformer {

    //usages for current rule
    HashSet<Name> usages = new HashSet<>();
    //usages for all original rules
    HashSet<Name> all = new HashSet<>();

    public Optimizer(Tree tree) {
        super(tree);
    }

    public static void optimize(Tree tree) {
        new Optimizer(tree).optimize();
    }


    public void optimize() {
        for (RuleDecl decl : tree.rules) {
            curRule = decl;
            if (decl.isOriginal) {
                decl.rhs.accept(this, null);
                all.add(decl.ref);
                all.addAll(usages);
            }
        }
        int count = tree.rules.size();
        for (Iterator<RuleDecl> it = tree.rules.iterator(); it.hasNext(); ) {
            RuleDecl decl = it.next();
            if (!all.contains(decl.ref)) {
                it.remove();
            }
        }
        int diff = count - tree.rules.size();
        if (diff > 0) {
            System.out.printf("removed %d unused rules\n", diff);
        }
    }

    @Override
    public Node visitName(Name name, Void arg) {
        if (name.isRule()) {
            if (usages.add(name)) {
                //recurse
                tree.getRule(name).rhs.accept(this, arg);
            }
        }
        return name;
    }
}
