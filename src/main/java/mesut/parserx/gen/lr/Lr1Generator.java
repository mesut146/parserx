package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Tree;


public class Lr1Generator extends LrDFAGen<Lr1ItemSet> {

    public Lr1Generator(Tree tree) {
        this.tree = tree;
    }

    @Override
    void prepare() {
        super.prepare();
        first.lookAhead.add(dollar);
    }

    @Override
    public Lr1ItemSet makeSet(LrItem item) {
        return new Lr1ItemSet(item, tree);
    }



}
