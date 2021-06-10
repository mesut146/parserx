package parser;

import common.Env;
import mesut.parserx.gen.ll.RecGenerator;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

public class LLRec {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("javaParser.g"));
        RecGenerator gen = new RecGenerator(tree);
        gen.generate();
    }

    @Test
    @Ignore
    public void test2() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("calc.g"));
        RecGenerator generator = new RecGenerator(tree);
        generator.className = "Parser";
        generator.lexerClass = "Lexer";
        generator.outDir = Env.dotDir().getAbsolutePath();
        generator.generate();
    }
}
