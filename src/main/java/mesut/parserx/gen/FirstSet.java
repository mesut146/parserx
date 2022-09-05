package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class FirstSet extends BaseVisitor<Void, Void> {
    Tree tree;
    LinkedHashSet<Name> res = new LinkedHashSet<>();
    Set<Name> rules = new HashSet<>();
    boolean recurse = true;
    boolean allowEpsilon = true;
    boolean lrEpsilon = false;

    public FirstSet(Tree tree) {
        this.tree = tree;
    }

    //first set graph
    public static void dot(Name ref, Tree tree, Writer writer) {
        var w = new PrintWriter(writer);
        w.println("digraph G{");
        w.println("rankdir = TB;");
        HashSet<Name> set = new HashSet<>();
        set.add(ref);
        dot(ref, tree, w, set);
        w.println("}");
        w.flush();
    }

    static void dot(Name ref, Tree tree, PrintWriter w, Set<Name> done) {
        var set = firstSetNoRec(ref, tree);
        for (var name : set) {
            if (name.isRule()) {
                w.printf("%s -> %s;", ref, name);
                if (done.add(ref)) {
                    dot(name, tree, w, done);
                }
            }
        }
    }

    public static boolean start(Node node, Name name, Tree tree) {
        return firstSet(node, tree).contains(name);
    }

    public static Set<Name> firstSet(Node node, Tree tree) {
        return firstSet(node, tree, false);
    }

    public static Set<Name> firstSet(Node node, Tree tree, boolean lrEpsilon) {
        var firstSet = new FirstSet(tree);
        firstSet.recurse = true;
        firstSet.lrEpsilon = lrEpsilon;
        node.accept(firstSet, null);
        return firstSet.res;
    }

    public static Set<Name> firstSetNoRec(Node node, Tree tree) {
        var firstSet = new FirstSet(tree);
        firstSet.recurse = false;
        node.accept(firstSet, null);
        return firstSet.res;
    }

    public static Set<Name> tokens(Node node, Tree tree) {
        Set<Name> res = new TreeSet<>();
        for (Name name : firstSet(node, tree)) {
            if (name.isToken) {
                res.add(name);
            }
        }
        return res;
    }

    public static boolean isEmpty(Node node, Tree tree) {
        return tokens(node, tree).isEmpty();
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
        for (var ch : or) {
            ch.accept(this, arg);
        }
        return null;
    }

    @Override
    public Void visitSequence(Sequence seq, Void arg) {
        for (var ch : seq) {
            ch.accept(this, arg);
            if (lrEpsilon || !canBeEmpty(ch, tree)) {
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
        if (!allowEpsilon && name.isRule() && isEmpty(name, tree)) {
            return null;
        }
        res.add(name);
        if (rules.add(name) && name.isRule() && recurse) {
            var list = tree.getRules(name);
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
            for (var ch : or) {
                if (ch.accept(this, arg)) return true;
            }
            return false;
        }

        @Override
        public Boolean visitSequence(Sequence seq, Void arg) {
            //all have to be empty
            for (var ch : seq) {
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
                for (var decl : tree.getRules(name)) {
                    if (decl.rhs.accept(this, arg)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


}
