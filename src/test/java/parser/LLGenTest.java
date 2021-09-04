package parser;

import common.Env;
import mesut.parserx.gen.Factor;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.VisitorGenerator;
import mesut.parserx.gen.ll.LLRec;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LLGenTest {

    @Test
    public void normalize() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Tree.makeTree(Env.getResFile("factor/group.g"));
        //new Normalizer(tree).normalize();
        //new Factor(tree).factorize();
        //System.out.println(tree);
        new LLRec(tree, options).gen();
    }

    @Test
    public void ast() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("model.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/resources/java/parser-jls.g"));
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        LLRec gen = new LLRec(tree, options);
        gen.gen();
        new LexerGenerator(tree, options).generate();
    }

    @Test
    public void factored() throws IOException {
        Tree tree = Env.makeRule("A: a b | B c | d e | f;\n" +
                "B: a x | d y | x;");
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        LLRec gen = new LLRec(tree, options);
        gen.gen();
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

    @Test
    public void visitor() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        options.parserClass = "Test";
        //Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        VisitorGenerator visitorGenerator = new VisitorGenerator(tree, options);
        visitorGenerator.generate();
    }
}
