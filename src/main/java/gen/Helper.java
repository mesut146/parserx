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
            Set<NameNode> ss = first(seq.get(0), tree);
            set.addAll(ss);
            //epsilon
        }
        return set;
    }
}
