package mesut.parserx.gen;

import mesut.parserx.nodes.*;

public class EpsilonRemover extends SimpleTransformer {
    Tree tree;

    @Override
    public Node transformRegex(RegexNode node, Node parent) {
        return super.transformRegex(node, parent);
    }

    @Override
    public Node transformEmpty(EmptyNode node, Node parent) {
        return super.transformEmpty(node, parent);
    }

}
