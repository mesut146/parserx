package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class PrepareTree extends SimpleTransformer {
    Tree tree;
    RuleDecl curRule;

    public PrepareTree(Tree tree) {
        this.tree = tree;
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        new PrepareTree(tree).check();
    }

    public void check() {
        for (RuleDecl decl : tree.rules) {
            curRule = decl;
            transformRule(decl);
        }
    }

    @Override
    public Node transformName(Name node, Node parent) {
        //rule or token
        if (tree.getRule(node) != null) {
            node.isToken = false;
        }
        else {
            TokenDecl decl = tree.getToken(node.name);
            if (decl == null) {
                throw new RuntimeException("invalid reference: " + node.name + " in " + curRule.name);
            }
            else {
                if (decl.isSkip) {
                    throw new RuntimeException("skip token inside production is not allowed");
                }
                node.isToken = true;
            }
        }
        return node;
    }

    @Override
    public Node transformString(StringNode node, Node parent) {
        String val = node.value;
        TokenDecl decl = tree.getTokenByValue(val);
        if (decl == null) {
            throw new RuntimeException("unknown string token: " + val + " in " + curRule.name);
        }
        //replace
        return decl.ref();
    }
}

