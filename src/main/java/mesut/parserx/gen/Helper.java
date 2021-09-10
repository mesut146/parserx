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
            if (name.astInfo.isFactored) return;
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

    public static void firstLoop(Node node, Tree tree, Set<Item> set) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.isFactored) return;
            if (name.isRule()) {
                firstLoop(tree.getRule(name).rhs, tree, set);
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                firstLoop(ch, tree, set);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            for (Node ch : seq) {
                firstLoop(ch, tree, set);
                if (!canBeEmpty(ch, tree)) {
                    break;
                }
            }
        }
        else if (node.isGroup()) {
            firstLoop(node.asGroup().node, tree, set);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isPlus() || regex.isStar()) {
                if (regex.node.isName()) {
                    if (regex.node.asName().isRule()) {
                        //?? first after
                    }
                    Item item = new Item();
                    item.isStar = regex.isStar();
                    item.isPlus = regex.isPlus();
                    item.name = regex.node.asName();
                }
                else {
                    firstLoop(node.asRegex().node, tree, set);
                }
            }
        }
        else if (node.isEpsilon()) {
            //set.add(((Epsilon) node));
        }
    }

    public static Map<Name, Integer> firstMap(Node node, Tree tree) {
        Map<Name, Integer> map = new HashMap<>();
        firstMap(node, null, tree, true, map);
        return map;
    }

    //freq of first set of regex
    public static void firstMap(Node node, Node parent, Tree tree, boolean rec, Map<Name, Integer> map) {
        if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.isFactored) return;
            if (map.containsKey(name)) {
                int i = map.get(name);
                if (i != Integer.MAX_VALUE) {//limit
                    map.put(name, i + 1);
                }
            }
            else {
                map.put(name, 1);
                if (rec && name.isRule()) {
                    firstMap(tree.getRule(name).rhs, null, tree, rec, map);
                }
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                firstMap(ch, node, tree, rec, map);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            for (Node ch : seq) {
                firstMap(ch, node, tree, rec, map);
                if (!canBeEmpty(ch, tree)) {
                    break;
                }
            }
        }
        else if (node.isGroup()) {
            firstMap(node.asGroup().node, node, tree, rec, map);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                //infinite
                Map<Name, Integer> tmp = new HashMap<>();
                firstMap(regex.node, node, tree, rec, tmp);
                for (Map.Entry<Name, Integer> entry : tmp.entrySet()) {
                    entry.setValue(Integer.MAX_VALUE);
                }
                map.putAll(tmp);
            }
            else {
                firstMap(regex.node, node, tree, rec, map);
            }
        }
    }

    public static List<Name> firstList(Node node, Tree tree) {
        return firstList(node, tree, true);
    }

    public static List<Name> firstList(Node node, Tree tree, boolean rec) {
        List<Name> list = new ArrayList<>();
        firstList(node, tree, rec, list);
        return list;
    }

    public static void firstList(Node node, Tree tree, boolean rec, List<Name> list) {
        if (node.isName()) {
            if (node.astInfo.isFactored) return;
            Name name = node.asName();
            if (name.isToken) {
                list.add(name);
            }
            else {
                //same ref
                //todo recursion
                list.add(name);
                if (rec) {
                    RuleDecl decl = tree.getRule(name);
                    firstList(decl.rhs, tree, rec, list);
                }
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                firstList(ch, tree, rec, list);
            }
        }
        else if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
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
            Regex r = node.asRegex();
            firstList(r.node, tree, rec, list);

        }
    }

    public static boolean canBeEmpty(Node node, Tree tree) {
        return canBeEmpty(node, tree, new HashSet<Name>());
    }

    public static boolean canBeEmpty(Node node, Tree tree, Set<Name> set) {
        if (node.isName()) {
            if (node.astInfo.isFactored) return true;//acts as epsilon
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
        SimpleTransformer transformer = new SimpleTransformer(tree) {
            @Override
            public Node transformName(Name node, Node parent) {
                if (node.isToken) {
                    Node rhs = tree.getToken(node.name).rhs;
                    if (rhs.isString()) {
                        return rhs;
                    }
                }
                return node;
            }
        };
        for (RuleDecl ruleDecl : tree.rules) {
            transformer.transformRule(ruleDecl);
        }
    }

    static class Item {
        boolean isStar;
        boolean isPlus;
        Name name;
    }
}
