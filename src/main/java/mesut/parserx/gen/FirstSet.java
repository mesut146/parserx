package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;

public class FirstSet extends BaseVisitor<Void, Void> {
    Tree tree;
    LinkedHashSet<Name> res = new LinkedHashSet<>();
    Set<Name> rules = new HashSet<>();
    Map<Name, LinkedHashSet<Name>> cache = new HashMap<>();
    List<RuleDecl> extraRules = new ArrayList<>();
    boolean recurse = true;
    boolean allowEpsilon = true;
    boolean lrEpsilon = false;

    public FirstSet(Tree tree) {
        this.tree = tree;
    }

    public static boolean start(Node node, Name name, Tree tree) {
        return firstSet(node, tree).contains(name);
    }

    public static Set<Name> firstSet(Node node, Tree tree) {
        return firstSet(node, tree, false, new ArrayList<>());
    }

    public static Set<Name> firstSet(Node node, Tree tree, boolean lrEpsilon) {
        return firstSet(node, tree, lrEpsilon, new ArrayList<>());
    }

    public static Set<Name> firstSet(Node node, Tree tree, boolean lrEpsilon, List<RuleDecl> extraRules) {
        var firstSet = new FirstSet(tree);
        firstSet.recurse = true;
        firstSet.lrEpsilon = lrEpsilon;
        firstSet.extraRules = extraRules;
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
        return tokens(node, tree, new ArrayList<>());
    }

    public static Set<Name> tokens(Node node, Tree tree, List<RuleDecl> extraRules) {
        var res = new TreeSet<Name>();
        for (Name name : firstSet(node, tree, false, extraRules)) {
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
        return EmptyChecker.canBeEmpty(node, tree);
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
        //cache algorithm is difficult since we need to track cur rule
//        if (cache.containsKey(name)) {
//            res.addAll(cache.get(name));
//            return null;
//        }
        res.add(name);
        if (!(rules.add(name) && name.isRule() && recurse)) return null;
        var list = tree.getRules(name);
        if (list.isEmpty()) {
            var extra = extraRules.stream().filter(e -> e.ref.equals(name)).findFirst();
            if (extra.isPresent()) {
                extra.get().rhs.accept(this, arg);
            }
            else {
                throw new RuntimeException("rule not found: " + name);
            }
        }
        else {
            for (var decl : list) {
                decl.rhs.accept(this, arg);
            }
        }
        return null;
    }

    public static class EmptyChecker extends BaseVisitor<Boolean, Void> {
        Set<Name> rules = new HashSet<>();
        Map<Name, Boolean> cache = new HashMap<>();
        Tree tree;

        public EmptyChecker(Tree tree) {
            this.tree = tree;
        }

        public static boolean canBeEmpty(Node node, Tree tree) {
            return node.accept(tree.emptyChecker, null);
        }

        @Override
        public Boolean visitFactored(Factored factored, Void arg) {
            return true;
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
            if (name.isToken) return false;
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            if (rules.add(name)) {//prevents recursion
                for (var decl : tree.getRules(name)) {
                    if (decl.rhs.accept(this, arg)) {
                        cache.put(name, true);
                        return true;
                    }
                }
            }
            cache.put(name, false);
            return false;
        }
    }


}
