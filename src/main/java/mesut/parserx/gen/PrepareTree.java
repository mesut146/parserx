package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class PrepareTree extends SimpleTransformer {
    Tree tree;

    public PrepareTree(Tree tree) {
        this.tree = tree;
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        new ReferenceChecker(tree).check();
    }

    //put back terminals as string nodes for good visuals
    public static void revert(final Tree tree) {
        final SimpleTransformer transformer = new SimpleTransformer() {
            @Override
            public Node transformName(Name node, Node parent) {
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
    public Node transformOr(Or node, Node parent) {
        for (Node ch : node) {
            if (Helper.canBeEmpty(ch, tree)) {
                throw new RuntimeException("epsilon inside alternation doesn't make sense,convert whole node into option");
            }
        }
        return super.transformOr(node, parent);
    }

    public static class ReferenceChecker extends SimpleTransformer {
        Tree tree;

        public ReferenceChecker(Tree tree) {
            this.tree = tree;
        }

        public void check() {
            for (RuleDecl decl : tree.rules) {
                transformRule(decl);
            }
        }

        @Override
        public Node transformName(Name node, Node parent) {
            if (tree.hasRule(node.name)) {
                node.isToken = false;
            }
            else {
                TokenDecl decl = tree.getToken(node.name);
                if (decl == null) {
                    throw new RuntimeException("invalid reference: " + node.name + " in " + parent);
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
                throw new RuntimeException("unknown string token: " + val + " in " + parent);
            }
            //replace
            return decl.ref();
        }
    }
}
