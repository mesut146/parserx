package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Set;

public class Helper {

    public static boolean startWith(RuleDecl rule, String name) {
        return first(rule.rhs, rule.tree, false).contains(new NameNode(name, false));
    }

    public static Node hasEps(OrNode or) {
        OrNode res = new OrNode();
        for (Node node : or) {
            if (!node.isEmpty()) {
                res.add(node);
            }
        }
        if (res.size() == or.size()) {
            return null;
        }
        return res.normal();
    }

    public static Set<NameNode> first(Node node, Tree tree, boolean rec) {
        Set<NameNode> set = new HashSet<>();
        first(node, tree, rec, set);
        return set;
    }

    //direct first set
    public static void first(Node node, Tree tree, boolean rec, Set<NameNode> set) {
        if (node.isName()) {
            NameNode name = node.asName();
            if (set.add(node.asName())) {
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
