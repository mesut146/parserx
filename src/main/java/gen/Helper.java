package gen;

import nodes.*;

import java.util.HashSet;
import java.util.Set;

public class Helper {

    public static boolean startWith(RuleDecl rule, String name) {
        return first(rule.rhs, rule.tree).contains(new NameNode(name, false));
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

    public static Set<NameNode> first(Node node, Tree tree) {
        Set<NameNode> set = new HashSet<>();
        if (node.isName()) {
            set.add(node.asName());
        }
        else if (node.isOr()) {
            for (Node ch : node.asOr()) {
                set.addAll(first(ch, tree));
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            for (Node ch : seq) {
                set.addAll(first(ch, tree));
                if (!canBeEmpty(ch, tree)) {
                    break;
                }
            }
        }
        else if (node.isGroup()) {
            set.addAll(first(node.asGroup().node, tree));
        }
        else if (node.isRegex()) {
            set.addAll(first(node.asRegex().node, tree));
        }
        return set;
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
                if (canBeEmpty(sequence.get(i), tree)) {
                    return true;
                }
            }
        }
        return false;
    }
}
