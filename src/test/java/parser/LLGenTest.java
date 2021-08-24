package parser;

import common.Env;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.LLGen;
import mesut.parserx.gen.ll.LLRec;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class LLGenTest {

    @Test
    public void ll() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("ll.g"));
        LLGen gen = new LLGen(tree);
        gen.gen();
    }

    @Test
    public void model() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("model.g"));
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        LLRec gen = new LLRec(tree, options);
        gen.gen();
        new LexerGenerator(tree, options).generate();
    }

    @Test
    public void all() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
        new LexerGenerator(tree, options).generate();
        LLRec gen = new LLRec(tree, options);
        gen.gen();
    }
}
