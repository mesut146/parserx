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
        if (tree.start != null && tree.getRule(tree.start) == null) {
            throw new RuntimeException("start rule declared but not defined");
        }
        transformAll();
    }

    void checkMode(TokenDecl decl) {
        if (decl.mode == null) return;
        if (decl.mode.equals("default") || decl.mode.equals("DEFAULT")) {
            return;
        }
        var found = false;
        for (var tb : tree.tokenBlocks) {
            for (var mb : tb.modeBlocks) {
                if (mb.name.equals(decl.mode)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new RuntimeException("unknown mode " + decl.mode);
        }
    }

    @Override
    public TokenDecl transformToken(TokenDecl decl) {
        checkMode(decl);
        return super.transformToken(decl);
    }

    @Override
    public Node visitName(Name name, Void parent) {
        //rule or token
        if (tree.getRule(name) != null) {
            name.isToken = false;
        }
        else {
            var decl = tree.getToken(name.name);
            if (decl == null) {
                throw new RuntimeException("invalid reference: " + name.name + " in " + getDecl());
            }
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
            var val = node.value;
            var decl = tree.getTokenByValue(val);
            if (decl == null) {
                throw new RuntimeException("unknown string token: " + val + " in " + getDecl());
            }
            //replace with token
            return decl.ref();
        }
        return node;
    }
}

