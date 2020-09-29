package gen;

import nodes.Node;
import nodes.OrNode;
import nodes.Tree;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simplifier {
    Tree tree;

    public Simplifier(Tree tree) {
        this.tree = tree;
    }


    public void mergeOrs() {
        Map<String, List<Node>> map = new HashMap<>();
        for (RuleDecl decl : tree.rules) {
            List<Node> list = map.get(decl.name);
            if (list == null) {
                list = new ArrayList<>();
                map.put(decl.name, list);
            }
            list.add(decl.rhs);
        }
        tree.rules.clear();
        for (Map.Entry<String, List<Node>> entry : map.entrySet()) {
            if (entry.getValue().size() == 1) {
                tree.rules.add(new RuleDecl(entry.getKey(), entry.getValue().get(0)));
            }
            else {
                tree.rules.add(new RuleDecl(entry.getKey(), new OrNode(entry.getValue())));
            }
        }

    }

}
