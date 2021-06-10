package mesut.parserx.gen.lr;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.nodes.Tree;

import java.io.File;

// lr(0)
public class Lr0Generator extends LRGen<Lr0ItemSet> {

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    @Override
    public Lr0ItemSet makeSet(LrItem item) {
        return new Lr0ItemSet(item, tree);
    }


    void writeSource() {

    }
}
