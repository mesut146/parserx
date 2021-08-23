package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.*;

public class Helper {

    public static Node trim(Sequence s) {
        return new Sequence(s.list.subList(1, s.size())).normal();
    }

    public static Node trim(Or s) {
        return new Sequence(s.list.subList(1, s.size())).normal();
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
            if (set.add(name)) {
                if (rec && name.isRule()) {
                    first(tree.getRule(name).rhs, tree, rec, set);
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
            first(node.asRegex().node, tree, rec, set);
        }
        else if (node.isEpsilon()) {
            set.add(((Epsilon) node));
        }
    }

    public static List<Name> firstList(Node node, Tree tree) {
        ArrayList<Name> list = new ArrayList<>();
        firstList(node, tree, true, list);
        return list;
    }

    //first list of regex,allows duplicates
    public static void firstList(Node node, Tree tree, boolean rec, List<Name> list) {
        if (node.isName()) {
            Name name = node.asName();
            if (!list.contains(name)) {
                list.add(name);
                if (rec && name.isRule()) {
                    firstList(tree.getRule(name).rhs, tree, rec, list);
                }
            }
            else {
                list.add(name);
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                firstList(ch, tree, rec, list);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            for (Node ch : seq) {
                firstList(ch, tree, rec, list);
                if (!canBeEmpty(ch, tree)) {
                    break;
                }
            }
        }
        else if (node.isGroup()) {
            firstList(node.asGroup().node, tree, rec, list);
        }
        else if (node.isRegex()) {
            firstList(node.asRegex().node, tree, rec, list);
        }
    }

    public static void follow(Name name, Tree tree, Set<Name> set) {
        if (set.contains(name)) return;
        for (RuleDecl rule : tree.rules) {
            Node rhs = rule.rhs;
            if (rhs.isSequence()) {
                Sequence s = rhs.asSequence();
                int i = s.indexOf(name);
                if (i == -1) continue;
                if (i < s.size() - 1) {
                    Name next = (Name) s.get(i + 1);
                    if (next.isToken) {
                        //R: a A b
                        //FO(A)=b
                        set.add(next);
                    }
                    else {
                        //R: a A B
                        //FO(A)=FI(B)
                        first(next, tree, true, set);
                    }
                }
                else {
                    //FO(B)=FO(A)
                    //A: aB
                    follow(rule.ref(), tree, set);
                }
            }
        }
    }

    public static boolean canBeEmpty(Node node, Tree tree) {
        return canBeEmpty(node, tree, new HashSet<Name>());
    }

    public static boolean canBeEmpty(Node node, Tree tree, Set<Name> set) {
        if (node.isName()) {
            if (node.asName().isRule() && set.add(node.asName())) {
                return canBeEmpty(tree.getRule(node.asName()).rhs, tree, set);
            }
        }
        else if (node.isGroup()) {
            return canBeEmpty(node.asGroup().node, tree, set);
        }
        else if (node.isRegex()) {
            Regex r = node.asRegex();
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
        final SimpleTransformer transformer = new SimpleTransformer() {
            @Override
            public Node transformName(Name node, Node parent) {
                if (node.isToken) {
                    Node rhs = tree.getToken(node.name).regex;
                    if (rhs.isString()) {
                        return rhs;
                    }
                }
                return super.transformName(node, parent);
            }
        };
        for (RuleDecl ruleDecl : tree.rules) {
            transformer.transformRule(ruleDecl);
        }
    }
}
