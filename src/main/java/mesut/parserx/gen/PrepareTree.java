package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class PrepareTree extends SimpleTransformer {
    Tree tree;
    RuleDecl curRule;
    TokenDecl curToken;

    public PrepareTree(Tree tree) {
        this.tree = tree;
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        new PrepareTree(tree).check();
    }

    public void check() {
        for (TokenDecl decl : tree.tokens) {
            curToken = decl;
            transformToken(decl);
        }
        curToken = null;
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
                throw new RuntimeException("invalid reference: " + node.name + " in " + getDecl());
            }
            else {
                if (decl.isSkip && curRule != null) {
                    throw new RuntimeException("skip token inside production is not allowed");
                }
                node.isToken = true;
                if (curToken != null) {
                    return decl.regex;
                }
            }
        }
        return node;
    }

    String getDecl() {
        return curRule != null ? curRule.name : curToken.tokenName;
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

