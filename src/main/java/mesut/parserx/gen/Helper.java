package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Helper {

    public static Node trim(Sequence s) {
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
                    first(tree.getRule(name.name).rhs, tree, rec, set);
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

    public static boolean canBeEmpty(Node node, Tree tree) { if (node.isName()) {
            if (node.asName().isRule()) {
                return canBeEmpty(tree.getRule(node.asName().name).rhs, tree);
            }
        }
        else if (node.isGroup()) {
            return canBeEmpty(node.asGroup().node, tree);
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            if (regex.isOptional() || regex.isStar()) {
                return true;
            }
            return canBeEmpty(regex.node, tree);
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                if (canBeEmpty(ch, tree)) {
                    return true;
                }
            }
        }
        else if (node.isSequence()) {
            Sequence sequence = node.asSequence();
            for (int i = 0; i < sequence.size(); i++) {
                if (!canBeEmpty(sequence.get(i), tree)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
