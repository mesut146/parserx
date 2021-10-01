package parser;

import common.Env;
import mesut.parserx.gen.lr.AstBuilderGen;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LrTest {

    static void dot(File dotFile) {
        try {
            Runtime.getRuntime().exec("dot -Tpng -O " + dotFile).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void dots(LrDFAGen gen, String name) throws IOException {
        File dot = Env.dotFile(Utils.newName(name, ".dot"));
        gen.writeDot(new PrintWriter(dot));
        dot(dot);
        dot.delete();

        File table = Env.dotFile(Utils.newName(name, "-table.dot"));
        gen.writeTableDot(new PrintWriter(table));
        dot(table);
        table.delete();
    }

    void checkLr(String grammar, String type) throws Exception {
        Tree tree = Env.tree(grammar);
        LrDFAGen generator = new LrDFAGen(tree, type);
        generator.generate();
        generator.checkAll();
    }

    @Ignore
    @Test
    public void lr0() throws Exception {
        checkLr("lr0/left.g", "lr0");
        checkLr("lr0/simple.g", "lr0");
    }

    @Test
    public void lr1() throws Exception {
        checkLr("lr1/lr1.g", "lr1");
    }

    @Test
    public void loop() throws Exception {
        Tree tree = Env.tree("lr1/factor-loop-right.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, "lr1");
        generator.generate();
        dots(generator, tree.file.getName());
    }

    @Test
    public void codeGen() throws Exception {
        Tree tree = Env.tree("lr1/lr1.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        CodeGen codeGen = new CodeGen(tree, "lr1");
        codeGen.gen();
        dots(codeGen.gen, tree.file.getName());
    }

    @Test
    public void prec() throws Exception {
        Tree tree = Env.tree("lr1/prec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, "lalr");
        generator.generate();
        generator.checkAll();
        dots(generator, tree.file.getName());
    }

    @Test
    public void assoc() throws Exception {
        Tree tree = Env.tree("lr1/assoc.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, "lalr");
        generator.generate();
        generator.checkAll();
        dots(generator, tree.file.getName());
    }

    @Test
    public void lookahead() throws Exception {
        Tree tree = Env.tree("lr1/la.g");
        //Tree tree = Env.tree("lr1/prec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, "lr1");
        generator.generate();
        dots(generator, tree.file.getName());
    }

    @Test
    @Ignore
    public void astBuilder() throws Exception {
        Tree tree = Env.tree("lr1/calc.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //todo or combine
        AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();
    }

    @Test
    public void astReal() throws Exception {
        LrTester.checkAst(Env.tree("lr1/calc.g"), "1+2", "1+2*3", "3^2*1^(3-1)");
    }

    @Test
    public void rec() throws Exception {
        Tree tree = Env.tree("lr1/rec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen dfaGen = new LrDFAGen(tree, "lr1");
        LrDFAGen.debug = true;
        dfaGen.generate();
        dfaGen.checkAll();
        dots(dfaGen, tree.file.getName());
    }

    @Test
    public void all() throws Exception {
        LrTester.check(Env.tree("lr0/simple.g"), "bb", "bab", "abab");
        LrTester.check(Env.tree("lr0/left.g"), "cb", "cab", "caab");
        LrTester.check(Env.tree("lr1/la.g"), "bb", "abab", "aabaaab");
        LrTester.check(Env.tree("lr1/lr1.g"), "aea", "beb", "aeb", "bea");
        LrTester.check(Env.tree("lr1/assoc.g"), "1+2", "1+2+3");
        LrTester.check(Env.tree("lr1/assoc2.g"), "1?2:3", "1?2:3?4:5");
        LrTester.check(Env.tree("lr1/prec.g"), "1.5+2*3.9440", "2*3+1", "2^3*5+1");
        LrTester.check(Env.tree("lr1/prec-unary.g"), "-1+3", "1+-6");
        LrTester.check(Env.tree("lr1/calc.g"), "1+2", "1*2", "1+2*3", "2*3+1", "1+2^3", "2*2^-3");
        LrTester.check(Env.tree("lr1/factor-loop-right.g"), "ac", "ab", "aac", "aab");
        LrTester.check(Env.tree("lr1/eps.g"), "c", "ac", "xc", "axc", "de");
        LrTester.check(Env.tree("lr1/rec.g"), "abc", "abd", "ababc");
    }

    @Test
    public void epsilon() throws Exception {
        Tree tree = Env.tree("lr1/eps.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen dfaGen = new LrDFAGen(tree, "lr1");
        dfaGen.generate();
        //dfaGen.checkAll();
        dots(dfaGen, tree.file.getName());
    }
}
