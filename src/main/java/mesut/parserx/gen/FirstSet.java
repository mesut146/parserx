package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirstSet extends BaseVisitor<Void, Void> {
    Tree tree;
    Set<Name> res = new HashSet<>();
    Set<Name> rules = new HashSet<>();

    public FirstSet(Tree tree) {
        this.tree = tree;
    }

    public static Set<Name> firstSet(Node node, Tree tree) {
        FirstSet firstSet = new FirstSet(tree);
        node.accept(firstSet, null);
        return firstSet.res;
    }

    public static Set<Name> tokens(Node node, Tree tree) {
        Set<Name> res = new HashSet<>();
        for (Name name : firstSet(node, tree)) {
            if (name.isToken) {
                res.add(name);
            }
        }
        return res;
    }

    public static boolean canBeEmpty(Node node, Tree tree) {
        return node.accept(new EmptyChecker(tree), null);
    }

    @Override
    public Void visitGroup(Group group, Void arg) {
        return group.node.accept(this, arg);
    }

    @Override
    public Void visitOr(Or or, Void arg) {
        for (Node ch : or) {
            ch.accept(this, arg);
        }
        return null;
    }

    @Override
    public Void visitSequence(Sequence seq, Void arg) {
        for (Node ch : seq) {
            ch.accept(this, arg);
            if (!canBeEmpty(ch, tree)) {
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitRegex(Regex regex, Void arg) {
        if (regex.astInfo.isFactored) return null;
        return regex.node.accept(this, arg);
    }

    @Override
    public Void visitName(Name name, Void arg) {
        if (name.astInfo.isFactored) return null;
        res.add(name);
        if (rules.add(name) && name.isRule()) {
            List<RuleDecl> list = tree.getRules(name);
            if (list.isEmpty()) {
                throw new RuntimeException("rule not found: " + name);
            }
            for (RuleDecl decl : list) {
                decl.rhs.accept(this, arg);
            }
        }
        return null;
    }

    static class EmptyChecker extends BaseVisitor<Boolean, Void> {
        Set<Name> rules = new HashSet<>();
        Tree tree;

        public EmptyChecker(Tree tree) {
            this.tree = tree;
        }

        @Override
        public Boolean visitRegex(Regex regex, Void arg) {
            if (regex.astInfo.isFactored) return true;
            if (regex.isPlus()) {
                return regex.node.accept(this, arg);
            }
            else {
                return true;
            }
        }

        @Override
        public Boolean visitOr(Or or, Void arg) {
            for (Node ch : or) {
                if (ch.accept(this, arg)) return true;
            }
            return false;
        }

        @Override
        public Boolean visitSequence(Sequence seq, Void arg) {
            //all have to be empty
            for (Node ch : seq) {
                if (!ch.accept(this, arg)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visitGroup(Group group, Void arg) {
            return group.node.accept(this, arg);
        }

        @Override
        public Boolean visitEpsilon(Epsilon epsilon, Void arg) {
            return true;
        }

        @Override
        public Boolean visitName(Name name, Void arg) {
            if (name.astInfo.isFactored) return true;
            if (name.isRule() && rules.add(name)) {
                for (RuleDecl decl : tree.getRules(name)) {
                    if (decl.rhs.accept(this, arg)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
