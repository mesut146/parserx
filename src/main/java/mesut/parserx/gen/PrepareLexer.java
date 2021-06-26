package mesut.parserx.gen;

import mesut.parserx.nodes.*;

//replaces references with regex contents
public class PrepareLexer extends SimpleTransformer {

    Tree tree;

    public PrepareLexer(Tree tree) {
        this.tree = tree;
    }

    public Tree prepare() {
        for (TokenDecl tokenDecl : tree.tokens) {
            transformToken(tokenDecl);
        }
        return tree;
    }

    @Override
    public Node transformName(Name node, Node parent) {
        TokenDecl tokenDecl = tree.getToken(node.name);
        if (tokenDecl == null) {
            throw new RuntimeException("invalid token: " + node.name);
        }
        return tokenDecl.regex;
    }
}
