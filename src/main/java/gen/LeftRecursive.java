package gen;

import nodes.Node;
import nodes.Tree;
import nodes.RuleDecl;

import java.util.ArrayList;
import java.util.List;

//remove left recursions
public class LeftRecursive {
    Tree tree;//must be bnf grammar

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
        List<Node> first = firstSequence(rhs);
        System.out.println("first=" + first);
    }

    List<Node> firstSequence(Node rhs) {
        List<Node> list = new ArrayList<>();

        if (rhs.isName()) {
            list.add(rhs);
            RuleDecl decl = tree.getRule(rhs.asName().name);
            list.addAll(firstSequence(decl.rhs));
        }
        else if (rhs.isSequence()) {

        }

        return list;
    }

    Node getFirst(RuleDecl decl, Node rhs) {
        if (rhs.isName() && !rhs.asName().isToken) {
            return rhs;
        }
        else if (rhs.isSequence()) {
            return getFirst(decl, rhs.asSequence().get(0));
        }
        else if (rhs.isOr()) {
            for (Node or : rhs.asOr()) {
                Node first = getFirst(decl, or);
                if (first != null) {
                    return first;
                }
            }

        }
        else if (rhs.isGroup()) {
            return getFirst(decl, rhs.asGroup().rhs);
        }

        return null;
    }
}
