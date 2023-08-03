package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

public class FirstSet extends BaseVisitor<Void, Void> {
    Tree tree;
    LinkedHashSet<Name> res = new LinkedHashSet<>();
    Set<Name> rules = new HashSet<>();
    List<RuleDecl> extraRules = new ArrayList<>();
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
        return firstSet.firstSet(node, lrEpsilon, extraRules);
    }

    public static Set<Name> tokens(Node node, Tree tree) {
        return tokens(node, tree, new ArrayList<>());
    }

    public static Set<Name> tokens(Node node, Tree tree, List<RuleDecl> extraRules) {
        var res = new TreeSet<Name>();
        for (var name : firstSet(node, tree, false, extraRules)) {
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

    public List<Name> firstSetSorted(Node node, boolean lrEpsilon) {
        return firstSet(node, lrEpsilon, new ArrayList<>())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public Set<Name> firstSet(Node node, boolean lrEpsilon, List<RuleDecl> extraRules) {
        this.res.clear();
        this.lrEpsilon = lrEpsilon;
        this.extraRules = extraRules;
        this.rules.clear();
        if (node.isName() && node.asName().isRule()) {
            //except itself
            //ruleStack.push(node.asName());
            //cache.put(node.asName(), new LinkedHashSet<>());
            tree.getRule(node.asName()).rhs.accept(this, null);
            //ruleStack.pop();
        } else {
            node.accept(this, null);
        }
        return res;
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
    public Void visitName(Name name, Void arg) {
        res.add(name);
        if (name.isToken) {
            return null;
        }

        if (!rules.add(name)) return null;
        var decl = tree.getRule(name);
        if (decl != null) {
            decl.rhs.accept(this, arg);
            return null;
        }
        var extra = extraRules.stream().filter(e -> e.ref.equals(name)).findFirst();
        if (extra.isPresent()) {
            extra.get().rhs.accept(this, arg);
        } else {
            throw new RuntimeException("rule not found: " + name);
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
            if (regex.isPlus()) {
                return regex.node.accept(this, arg);
            } else {
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
        public Boolean visitEpsilon(Epsilon epsilon, Void arg) {
            return true;
        }


        @Override
        public Boolean visitName(Name name, Void arg) {
            if (name.isToken) return false;
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            if (rules.add(name)) {//prevents recursion
                var decl = tree.getRule(name);
                if (decl.rhs.accept(this, arg)) {
                    cache.put(name, true);
                    return true;
                }
            }
            cache.put(name, false);
            return false;
        }
    }


}
