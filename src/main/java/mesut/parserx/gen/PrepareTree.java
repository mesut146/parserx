package mesut.parserx.gen;

import mesut.parserx.nodes.*;

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

    //put back terminals as string nodes for good visuals
    public static void revert(final Tree tree) {
        final SimpleTransformer transformer = new SimpleTransformer() {
            @Override
            public Node transformName(NameNode node, Node parent) {
                if (node.isToken) {
                    Node rhs = tree.getToken(node.name).regex;
                    if (rhs.isString()) {
                        return rhs;
                    }
                }
                return super.transformName(node, parent);
            }
        };
        for (RuleDecl ruleDecl : tree.rules) {
            transformer.transformRule(ruleDecl);
        }
    }

    @Override
    public Node transformName(NameNode node, Node parent) {
        if (tree.hasRule(node.name)) {
            node.isToken = false;
        }
        else {
            if (tree.getToken(node.name) == null) {
                throw new RuntimeException("invalid reference: " + node.name + " in " + parent);
            }
            else {
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
            throw new RuntimeException("unknown string token: " + val + " in " + parent);
        }
        //replace
        return decl.ref();
    }
}
