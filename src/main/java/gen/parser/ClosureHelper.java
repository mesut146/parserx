package gen.parser;

import nodes.Node;
import nodes.Tree;
import nodes.RuleDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClosureHelper {
    Tree tree;

    public ClosureHelper(Tree tree) {
        this.tree = tree;
    }

    public void all() {
        for (RuleDecl decl : tree.rules) {
            System.out.printf("closure(%s) = %s\n", decl, closureDecl(decl, new HashMap<RuleDecl, Set<Node>>()));
        }
    }

    Set<Node> closureDecl(RuleDecl decl, Map<RuleDecl, Set<Node>> map) {
        if (!map.containsKey(decl)) {
            return closure(decl.rhs, new HashSet<Node>());
        }
        return closure(decl.rhs, map.get(decl));
    }

    Set<Node> closure(Node node, Set<Node> list) {
        //System.out.println("node=" + node);
        if (node.isName()) {
            if (node.asName().isToken) {
                list.add(node);
            }
            else {
                closure(tree.getRule(node.asName().name).rhs, list);
            }
        }
        else if (node.isSequence()) {
            closure(node.asSequence().get(0), list);
        }
        else if (node.isOr()) {
            for (Node first : node.asOr()) {
                closure(first, list);
            }
        }
        return list;
    }
}
