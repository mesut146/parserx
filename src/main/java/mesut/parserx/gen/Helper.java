package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Helper {

    //extract epsilon from or
    public static Node hasEps(OrNode or) {
        OrNode res = new OrNode();
        for (Node node : or) {
            if (!node.isEmpty()) {
                res.add(node);
            }
        }
        if (res.size() == or.size()) {
            //no epsilon
            return null;
        }
        return res.normal();
    }

    public static Set<NameNode> first(Node node, Tree tree, boolean rec) {
        Set<NameNode> set = new HashSet<>();
        first(node, tree, rec, set);
        return set;
    }

    public static Set<NameNode> first(Node node, Tree tree, boolean rec, boolean allowRules, boolean allowTokens) {
        Set<NameNode> set = new HashSet<>();
        first(node, tree, rec, set);
        for (Iterator<NameNode> it = set.iterator(); it.hasNext(); ) {
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
    public static void first(Node node, Tree tree, boolean rec, Set<NameNode> set) {
        if (node.isName()) {
            NameNode name = node.asName();
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

    public static void follow(NameNode name, Tree tree, Set<NameNode> set) {
        if (set.contains(name)) return;
        for (RuleDecl rule : tree.rules) {
            Node rhs = rule.rhs;
            if (rhs.isSequence()) {
                Sequence s = rhs.asSequence();
                int i = s.indexOf(name);
                if (i == -1) continue;
                if (i < s.size() - 1) {
                    NameNode next = (NameNode) s.get(i + 1);
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
        if (node.isEmpty()) {
            return true;
        }
        else if (node.isName()) {
            if (node.asName().isRule()) {
                return canBeEmpty(tree.getRule(node.asName().name), tree);
            }
        }
        else if (node.isGroup()) {
            return canBeEmpty(node.asGroup().node, tree);
        }
        else if (node.isRegex()) {
            RegexNode regexNode = node.asRegex();
            if (regexNode.isOptional() || regexNode.isStar()) {
                return true;
            }
            return canBeEmpty(regexNode.node, tree);
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
