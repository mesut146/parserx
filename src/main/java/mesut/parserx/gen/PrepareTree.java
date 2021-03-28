package mesut.parserx.gen;

import mesut.parserx.nodes.*;
import mesut.parserx.nodes.RuleDecl;

public class PrepareTree extends SimpleTransformer {
    Tree tree;

    public PrepareTree(Tree tree) {
        this.tree = tree;
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        PrepareTree prepareTree = new PrepareTree(tree);
        for (RuleDecl rule : tree.rules) {
            prepareTree.transformRule(rule);
        }
    }

    @Override
    public Node transformName(NameNode node, Node parent) {
        if (node.isToken) {
            if (tree.getToken(node.name) == null) {
                throw new RuntimeException("invalid token: " + node.name);
            }
        }
        else {
            if (tree.hasRule(node.name)) {
                node.isToken = false;
            }
            else {
                if (tree.getToken(node.name) == null) {
                    throw new RuntimeException("invalid reference: " + node.name);
                }
                else {
                    node.isToken = true;
                }
            }
        }
        return node;
    }

    @Override
    public Node transformString(StringNode node, Node parent) {
        String val = node.value;
        TokenDecl decl = tree.getTokenByValue(val);
        if (decl == null) {
            throw new RuntimeException("unknown string token: " + val);
        }
        //replace
        return decl.makeReference();
    }
}
