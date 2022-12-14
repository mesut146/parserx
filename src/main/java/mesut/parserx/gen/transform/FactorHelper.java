package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class FactorHelper {

    //can start with other than sym
    public static boolean hasZero(Node rhs, final Name sym, final Tree tree) {
        Set<Name> done = new HashSet<>();
        BaseVisitor<Boolean, Void> checker = new ZeroVisitor(done, sym, tree);
        return rhs.accept(checker, null);
    }

    public static boolean hasCommon(Node a, Node b, Tree tree) {
        Set<Name> s1 = FirstSet.tokens(a, tree);
        Set<Name> s2 = FirstSet.tokens(b, tree);
        Set<Name> common = new LinkedHashSet<>(s1);
        common.retainAll(s2);
        return !common.isEmpty();
    }

    public static Name common(Set<Name> s1, Set<Name> s2, Tree tree) {
        Set<Name> common = new LinkedHashSet<>(s1);
        common.retainAll(s2);
        if (common.isEmpty()) return null;

        Name res = null;
        int max = 0;
        for (Name name : common) {
            if (name.isRule()) {
                //most token count wins
                int cur = FirstSet.tokens(name, tree).size();
                if (cur > max) {
                    res = name;
                    max = cur;
                }
            }
            else if (res == null) {
                res = name;
            }
        }
        return res;
    }


    private static class ZeroVisitor extends BaseVisitor<Boolean, Void> {
        private final Set<Name> done;
        private final Name sym;
        private final Tree tree;

        public ZeroVisitor(Set<Name> done, Name sym, Tree tree) {
            this.done = done;
            this.sym = sym;
            this.tree = tree;
        }

        @Override
        public Boolean visitName(Name name, Void arg) {
            if (done.contains(name)) return false;
            done.add(name);
            if (name.isToken) {
                return !name.equals(sym);
            }
            else {
                if (name.equals(sym)) {
                    return false;
                }
                else {
                    return tree.getRule(name).rhs.accept(this, arg);
                }
            }
        }

        @Override
        public Boolean visitRegex(Regex regex, Void arg) {
            return regex.node.accept(this, arg);
        }

        @Override
        public Boolean visitSequence(Sequence seq, Void arg) {
            Node a = seq.first();
            if (seq.size() == 1) {
                return a.accept(this, arg);
            }

            Node b = Helper.trim(seq);
            if (FirstSet.start(a, sym, tree)) {
                if (a.accept(this, arg)) return true;
                return FirstSet.canBeEmpty(a, tree) && b.accept(this, arg);
            }
            else {
                if (FirstSet.canBeEmpty(a, tree)) {
                    return b.accept(this, arg);
                }
                else {
                    return true;
                }
            }
        }

        @Override
        public Boolean visitOr(Or or, Void arg) {
            for (Node ch : or) {
                if (ch.accept(this, arg)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean visitGroup(Group group, Void arg) {
            return group.node.accept(this, arg);
        }
    }
}
