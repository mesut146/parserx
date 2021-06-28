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
            if (node.isToken) {
                checkToken(node, parent);
            }
            else {
                //rule or token
                if (tree.getRule(node) != null) {
                    node.isToken = false;
                }
                else {
                    checkToken(node, parent);
                }
            }
            return node;
        }

        void checkToken(Name node, Node parent) {
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
