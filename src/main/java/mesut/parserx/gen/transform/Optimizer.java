package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Optimizer {
    Tree tree;

    public Optimizer(Tree tree) {
        this.tree = tree;
    }

    public static void optimize(Tree tree) {
        new Optimizer(tree).optimize();
    }

    public void optimize() {
        HashSet<Name> all = new HashSet<>();
        for (RuleDecl decl : tree.rules) {
            if (tree.isOriginal(decl.ref)) {
                all.add(decl.ref);
                all.addAll(UsageCollector.collect(decl.rhs, tree));
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

    public static class UsageCollector extends Transformer {
        Set<Name> usages = new HashSet<>();

        public UsageCollector(Tree tree) {
            super(tree);
        }

        public static Set<Name> collect(Node node, Tree tree) {
            UsageCollector collector = new UsageCollector(tree);
            node.accept(collector, null);
            return collector.usages;
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

}
