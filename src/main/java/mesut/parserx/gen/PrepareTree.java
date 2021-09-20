package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class PrepareTree extends SimpleTransformer {

    public PrepareTree(Tree tree) {
        super(tree);
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        new PrepareTree(tree).check();
    }

    public void check() {
        transformAll();
        for (Assoc assoc : tree.assocList) {
            for (Name name : assoc.list) {
                if (tree.getToken(name.name) != null) {
                    name.isToken = true;
                }
                else if (tree.getRule(name.name) != null) {
                    name.isToken = false;
                }
                else {
                    throw new RuntimeException("invalid reference: " + name + " in assoc");
                }
            }
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
                throw new RuntimeException("invalid reference: " + node.name + " in " + getDecl());
            }
            else {
                if (decl.isSkip && curRule != null) {
                    throw new RuntimeException("skip token inside production is not allowed");
                }
                node.isToken = true;
                if (curToken != null) {
                    return decl.rhs;
                }
            }
        }
        return node;
    }

    String getDecl() {
        return curRule != null ? curRule.name : curToken.name;
    }

    @Override
    public Node transformString(StringNode node, Node parent) {
        if (node.value.isEmpty()) {
            System.out.println("empty string replaced by epsilon in " + getDecl());
            return new Epsilon();
        }
        if (curRule != null) {
            String val = node.value;
            TokenDecl decl = tree.getTokenByValue(val);
            if (decl == null) {
                throw new RuntimeException("unknown string token: " + val + " in " + getDecl());
            }
            //replace
            return decl.ref();
        }
        return node;
    }
}

