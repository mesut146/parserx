package gen;

import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosureHelper {
    Tree tree;

    public ClosureHelper(Tree tree) {
        this.tree = tree;
    }

    public void all() {
        int i = 0;
        for (RuleDecl decl : tree.rules) {
            decl.index = i;
            System.out.println(closureDecl(decl, new HashMap<RuleDecl, List<Node>>()));
        }
    }

    List<Node> closureDecl(RuleDecl decl, Map<RuleDecl, List<Node>> map) {
        if (!map.containsKey(decl)) {
            return closure(decl.rhs, new ArrayList<Node>());
        }
        return closure(decl.rhs, map.get(decl));
    }

    List<Node> closure(Node node, List<Node> list) {
        if (node.isName()) {
            if (node.asName().isToken) {
                list.add(node);
            }
            else {
                closure(tree.getRule(node.asName().name).rhs, list);
            }
        }
        else if (node.isSequence()) {
            closure(node.asSequence().list.get(0), list);
        }
        else if (node.isOr()) {
            for (Node first : node.asOr()) {
                closure(first, list);
            }
        }
        return list;
    }
}
