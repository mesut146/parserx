package parser;

import common.Env;
import mesut.parserx.gen.lr.AstBuilderGen;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LrTest {

    public static void dots(LrDFAGen gen, String name) throws IOException {
        File dot = Env.dotFile(Utils.newName(name, ".dot"));
        gen.writeDot(new PrintWriter(dot));
        Env.dot(dot);

        File table = Env.dotFile(Utils.newName(name, "-table.dot"));
        gen.writeTableDot(new PrintWriter(table));
        Env.dot(table);
    }

    private void dots(String res) throws IOException {
        Tree tree = Env.tree(res);
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, LrType.LR1);
        generator.generate();
        generator.checkAndReport();
        dots(generator, tree.file.getName());
    }

    @Test
    @Ignore
    public void java() throws Exception {
        Tree tree = Env.tree("java/parser-jls-eps.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen.debug = true;
        LrDFAGen generator = new LrDFAGen(tree, LrType.LR1);
        generator.generate();
        dots(generator, tree.file.getName());
    }

    @Test
    public void codeGen() throws Exception {
        Tree tree = Env.tree("lr1/lr1.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        CodeGen codeGen = new CodeGen(tree, LrType.LR1);
        codeGen.gen();
        dots(codeGen.gen, tree.file.getName());
    }

    @Test
    public void lookahead() throws Exception {
        Tree tree = Env.tree("lr1/la.g");
        //Tree tree = Env.tree("lr1/prec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen generator = new LrDFAGen(tree, LrType.LR1);
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
    public void testStar() throws Exception {
        LrDFAGen.debug = true;
        //dots("lr1/regex.g");
        LrTester.check(Env.tree("lr1/regex.g"), "ccc","bbbccc","accc","abbbccc");
        //LrTester.check(Env.tree("lr1/regex.g"), "x","aaax");
    }


    @Test
    public void all() throws Exception {
        LrTester.check(Env.tree("lr1/lr1.g"), LrType.LR1, "aea", "beb", "aeb", "bea");
        LrTester.check(Env.tree("lr1/left.g"), "x", "xab", "xabac");
        LrTester.check(Env.tree("lr1/la.g"), "bb", "abab", "aabaaab");
        LrTester.check(Env.tree("lr1/assoc.g"), "1+2", "1+2+3");
        LrTester.check(Env.tree("lr1/assoc2.g"), "1?2:3", "1?2:3?4:5");
        LrTester.check(Env.tree("lr1/pred.g"), "1+2*3", "2*3+1", "2^3*5+1");
        LrTester.check(Env.tree("lr1/prec-unary.g"), "-1+3", "1+-6");
        LrTester.check(Env.tree("lr1/calc.g"), "1+2", "1*2", "1+2*3", "2*3+1", "1+2^3", "2*2^-3");
        LrTester.check(Env.tree("lr1/factor-loop-right.g"), "ac", "ab", "aac", "aab");
        LrTester.check(Env.tree("lr1/rec.g"), "abc", "abd", "ababc");
        //LrTester.check(Env.tree("lr1/regex.g"), "ax","aaax");
        //LrTester.check(Env.tree("lr1/eps.g"), "c", "ac", "xc", "axc", "de");//todo
        //LrTester.check(Env.tree("lr1/eps.g"), "x","ax");//todo
    }

    @Test
    public void epsilon() throws Exception {
        Tree tree = Env.tree("lr1/eps.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen dfaGen = new LrDFAGen(tree, LrType.LR1);
        dfaGen.generate();
        dfaGen.checkAndReport();
        dots(dfaGen, tree.file.getName());
    }
}
