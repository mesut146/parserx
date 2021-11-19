package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class PrepareTree extends Transformer {

    public PrepareTree(Tree tree) {
        super(tree);
    }

    //check rule , token, string references
    public static void checkReferences(Tree tree) {
        new PrepareTree(tree).check();
    }

    public void check() {
        transformAll();
    }

    @Override
    public Node visitName(Name name, Void parent) {
        //rule or token
        if (tree.getRule(name) != null) {
            name.isToken = false;
        }
        else {
            TokenDecl decl = tree.getToken(name.name);
            if (decl == null) {
                throw new RuntimeException("invalid reference: " + name.name + " in " + getDecl());
            }
            else {
                if (decl == curToken) {
                    throw new RuntimeException("recursive token reference is not allowed: " + decl);
                }
                if (decl.isSkip && curRule != null) {
                    throw new RuntimeException("skip token inside production is not allowed");
                }
                name.isToken = true;
                if (curToken != null) {
                    return decl.rhs;
                }
            }
        }
        return name;
    }

    String getDecl() {
        return curRule != null ? curRule.getName() : curToken.name;
    }

    @Override
    public Node visitString(StringNode node, Void parent) {
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
            //replace with token
            return decl.ref();
        }
        return node;
    }
}

