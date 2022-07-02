package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LaFinder extends BaseVisitor<Void, Void> {

    public Tree tree;
    Set<Name> set = new HashSet<>();
    static Map<Name, Set<Name>> done = new HashMap<>();
    Name ref;

    public LaFinder(Tree tree) {
        this.tree = tree;
    }

    public static Set<Name> computeLa(Name ref, Tree tree) {
        LaFinder finder = new LaFinder(tree);
        finder.ref = ref;
        if (done.containsKey(ref)) return done.get(ref);
        done.put(ref, finder.set);
        if (ref.equals(tree.start)) {
            finder.set.add(LLDfaBuilder.dollar);
        }
        for (RuleDecl decl : tree.rules) {
            //if (decl.ref.equals(ref)) continue;
            finder.curRule = decl;
            decl.rhs.accept(finder, null);
        }
        return finder.set;
    }

    @Override
    public Void visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            Node ch = seq.get(i);
            boolean match = false;
            if (ch.equals(ref)) {
                if (i < seq.size() - 1) {
                    Sequence rest = new Sequence(seq.list.subList(i + 1, seq.size()));
                    set.addAll(FirstSet.tokens(rest, tree));
                    if (FirstSet.canBeEmpty(rest, tree)) {
                        set.addAll(computeLa(curRule.ref, tree));
                    }
                }
                match = true;
            }
            else if (ch.isRegex() && ch.asRegex().node.equals(ref)) {
                match = true;
                Regex regex = ch.asRegex();
                if (!regex.isOptional()) {
                    set.addAll(FirstSet.tokens(regex.node, tree));
                }
                if (i < seq.size() - 1) {
                    Sequence rest = new Sequence(seq.list.subList(i + 1, seq.size()));
                    if (FirstSet.canBeEmpty(rest, tree)) {
                        set.addAll(computeLa(curRule.ref, tree));
                    }
                    set.addAll(FirstSet.tokens(rest, tree));
                }
            }
            if (match && i == seq.size() - 1) {
                set.addAll(computeLa(curRule.ref, tree));
            }
        }
        return null;
    }
}
