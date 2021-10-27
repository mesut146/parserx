package mesut.parserx.gen;

import mesut.parserx.gen.transform.Factor;
import mesut.parserx.nodes.*;

import java.util.*;

public class Helper {

    public static Node trim(Sequence s) {
        return new Sequence(s.list.subList(1, s.size())).normal();
    }

    public static Node trim(Or s) {
        return new Or(s.list.subList(1, s.size())).normal();
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

    /*private static boolean isEpsilon(Node node, Tree tree) {
        return isEpsilon(node, tree, new HashMap<Name, Boolean>());
    }*/

    private static boolean isEpsilon(Node node, Tree tree, Map<Name, Boolean> set) {
        if (node.isEpsilon()) {
            return true;
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.astInfo.isFactored) {
                return true;
            }
            if (name.isRule()) {
                Boolean val = set.get(name);
                if (val == null) {

                }
                else {

                }
                /*if (set.add(name)) {
                    RuleDecl decl = tree.getRule(name);
                    return isEpsilon(decl.rhs, tree, set);
                }
                else {
                    //todo??
                }*/
            }
            else {
                return false;
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                if (!isEpsilon(ch, tree, set)) {
                    return false;
                }
            }
            return true;
        }
        else if (node.isSequence()) {
            for (Node ch : node.asSequence()) {
                if (!isEpsilon(ch, tree, set)) {
                    return false;
                }
            }
            return true;
        }
        else if (node.isGroup()) {
            return isEpsilon(node.asGroup().node, tree, set);
        }
        else if (node.isRegex()) {
            return isEpsilon(node.asRegex().node, tree, set);
        }
        return false;
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
        firstMap(node, tree, map);
        return map;
    }

    //freq of first set of regex
    public static void firstMap(Node node, Tree tree, Map<Name, Integer> map) {
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
                if (name.isRule()) {
                    firstMap(tree.getRule(name).rhs, tree, map);
                }
            }
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                firstMap(ch, tree, map);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            Node a = seq.first();
            Node b = trim(seq);
            //todo overflow
            Map<Name, Integer> amap = firstMap(a, tree);
            if (canBeEmpty(a, tree)) {
                //a b | b
                Map<Name, Integer> bmap = firstMap(b, tree);
                map.putAll(amap);
                map.putAll(bmap);
            }
            else {
                map.putAll(amap);
            }
        }
        else if (node.isGroup()) {
            firstMap(node.asGroup().node, tree, map);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                //infinite
                Map<Name, Integer> tmp = new HashMap<>();
                firstMap(regex.node, tree, tmp);
                for (Map.Entry<Name, Integer> entry : tmp.entrySet()) {
                    Factor factor = new Factor(tree);
                    Factor.PullInfo info = factor.pull(regex.node, entry.getKey());
                    if (info.one.isEpsilon() || info.one.astInfo.isFactored) {
                        entry.setValue(Integer.MAX_VALUE);
                    }
                    else {
                        //once
                    }
                    for (RuleDecl decl : factor.declSet) {
                        tree.rules.remove(decl);
                    }
                }
                map.putAll(tmp);
            }
            else {
                firstMap(regex.node, tree, map);
            }
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
