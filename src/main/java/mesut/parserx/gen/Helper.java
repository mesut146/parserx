package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Helper {

    public static Node trim(Sequence s) {
        List<Node> list = s.list.subList(1, s.size());
        if (list.size() == 1) {
            return list.get(0);
        }
        return new Sequence(list);
    }

    public static Node trim(Or s) {
        List<Node> list = s.list.subList(1, s.size());
        if (list.size() == 1) return list.get(0);
        return new Or(list);
    }

    public static boolean start(Node node, Name name, Tree tree) {
        return first(node, tree, true).contains(name);
    }

    public static Set<Name> first(Node node, Tree tree, boolean rec) {
        Set<Name> set = new HashSet<>();
        first(node, tree, rec, set);
        return set;
    }

    public static Set<Name> first(Node node, Tree tree, boolean rec, boolean allowRules, boolean allowTokens) {
        Set<Name> set = new HashSet<>();
        first(node, tree, rec, set);
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            if (it.next().isToken) {
                if (!allowTokens) {
                    it.remove();
                }
            }
            else {
                if (!allowRules) {
                    it.remove();
                }
            }
        }
        return set;
    }

    //first set of regex
    public static void first(Node node, Tree tree, boolean rec, Set<Name> set) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.isFactored) return;
            if (set.add(name)) {
                if (rec && name.isRule()) {
                    List<RuleDecl> list = tree.getRules(name);
                    if (list.isEmpty()) {
                        throw new RuntimeException("rule not found: " + name);
                    }
                    for (RuleDecl decl : list) {
                        first(decl.rhs, tree, rec, set);
                    }

                }
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                first(ch, tree, rec, set);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            for (Node ch : seq) {
                first(ch, tree, rec, set);
                if (!canBeEmpty(ch, tree)) {
                    break;
                }
            }
        }
        else if (node.isGroup()) {
            first(node.asGroup().node, tree, rec, set);
        }
        else if (node.isRegex()) {
            if (node.astInfo.isFactored) return;
            first(node.asRegex().node, tree, rec, set);
        }
    }


    public static boolean canBeEmpty(Node node, Tree tree) {
        return canBeEmpty(node, tree, new HashSet<Name>());
    }

    public static boolean canBeEmpty(Node node, Tree tree, Set<Name> set) {
        if (node.isName()) {
            if (node.astInfo.isFactored) return true;//acts as epsilon
            Name name = node.asName();
            if (name.isRule() && set.add(name)) {
                for (RuleDecl decl : tree.getRules(name)) {
                    if (canBeEmpty(decl.rhs, tree, set)) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if (node.isGroup()) {
            return canBeEmpty(node.asGroup().node, tree, set);
        }
        else if (node.isRegex()) {
            Regex r = node.asRegex();
            if (r.astInfo.isFactored) return true;
            if (r.isOptional() || r.isStar()) {
                return true;
            }
            return canBeEmpty(r.node, tree, set);
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                if (canBeEmpty(ch, tree, set)) {
                    return true;
                }
            }
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            for (int i = 0; i < s.size(); i++) {
                if (!canBeEmpty(s.get(i), tree, set)) {
                    return false;
                }
            }
            return true;
        }
        return node.isEpsilon();
    }

    //put back terminals as string nodes for good visuals
    public static void revert(final Tree tree) {
        Transformer transformer = new Transformer() {
            @Override
            public Node visitName(Name name, Void arg) {
                if (name.isToken) {
                    Node rhs = tree.getToken(name.name).rhs;
                    if (rhs.isString()) {
                        return rhs;
                    }
                }
                return name;
            }
        };
        for (RuleDecl ruleDecl : tree.rules) {
            ruleDecl.rhs = ruleDecl.rhs.accept(transformer, null);
        }
    }
}
