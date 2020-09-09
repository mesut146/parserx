package gen;

import nodes.Node;
import nodes.Tree;
import rule.RuleDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//remove left recursions
public class LeftRecursive {
    Tree tree;//must be bnf grammar
    Map<RuleDecl, List<Node>> firstMap = new HashMap<>();

    public LeftRecursive(Tree tree) {
        this.tree = tree;
    }

    public void transform() {
        for (RuleDecl decl : tree.rules) {
            transform(decl);
        }
    }

    private void transform(RuleDecl decl) {
        //handle direct
        Node rhs = decl.rhs;
        Node first = getFirst(decl, rhs);
        addFirst(decl, first);
    }

    private void addFirst(RuleDecl decl, Node first) {
        if (first != null) {
            List<Node> list = firstMap.get(decl);
            if (list == null) {
                list = new ArrayList<>();
                firstMap.put(decl, list);
            }
            list.add(first);
        }
    }

    Node getFirst(RuleDecl decl, Node rhs) {
        if (rhs.isName() && !rhs.asName().isToken) {
            return rhs;
        }
        else if (rhs.isSequence()) {
            return getFirst(decl, rhs.asSequence().list.get(0));
        }
        else if (rhs.isOr()) {
            for (Node or : rhs.asOr()) {
                addFirst(decl, getFirst(decl, or));
            }
        }
        else if (rhs.isGroup()) {
            return getFirst(decl, rhs.asGroup().rhs);
        }

        return null;
    }
}
