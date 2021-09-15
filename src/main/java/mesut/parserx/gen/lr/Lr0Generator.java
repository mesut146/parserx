package mesut.parserx.gen.lr;

import mesut.parserx.nodes.Tree;

public class Lr0Generator extends LRTableGen<Lr0ItemSet> {

    public Lr0Generator(Tree tree) {
        this.tree = tree;
    }

    @Override
    public Lr0ItemSet makeSet(LrItem item) {
        return new Lr0ItemSet(item, tree);
    }
}
