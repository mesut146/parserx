package parser;

import common.Env;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.lr.*;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LrTest {

    void dot(File dotFile) {
        try {
            Runtime.getRuntime().exec("dot -Tpng -O " + dotFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void dots(LrDFAGen<?> gen, String name) throws IOException {
        gen.writeTableDot(new PrintWriter(Env.dotFile(Utils.newName(name, "-table.dot"))));

        dot(gen.tableDotFile());

        File dot = Env.dotFile(Utils.newName(name, ".dot"));
        gen.writeDot(new PrintWriter(dot));
        dot(dot);
    }

    @Ignore
    @Test
    public void lr0() throws Exception {
        File file;
        //file = Env.getCalc();
        //file = Env.getResFile(gr);
        //file = Env.getResFile("lr0/left.g");
        //file = Env.getResFile("lr1/calc2.g");
        //file = Env.getFile2("lr1/simple.g");
        //file = Env.getResFile("rec/cyc.g");
        //file = Env.getFile2("javaParser.g");
        Tree tree = Env.tree("lr1/calc3.g");
        Lr0Generator generator = new Lr0Generator(tree);
        generator.generate();
        dots(generator, tree.file.getName());
    }

    @Test
    @Ignore
    public void lr1() throws Exception {
        //file = Env.getFile2("lr1/calc2.g");
        //file = Env.getFile2("lr1/simple.g");
        //Tree tree = Env.tree("lr0/simple.g");
        //Tree tree = Env.tree("lr1/calc3.g");
        //Tree tree = Env.tree("lr1/calc.g");
        //Tree tree = Env.tree("java/parser-jls-eps.g");
        //Tree tree = Env.tree("lr1/lr1.g");
        //file = Env.getResFile("rec/cyc.g");
        Tree tree = Env.tree("lr1/factor-loop-right.g");
        //Lr1ItemSet.mergeLa = true;
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Lr1Generator generator = new Lr1Generator(tree);
        //generator.merge = true;
        //EpsilonTrimmer.trim(tree);
        generator.generate();
        //generator.checkAll();
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
    public void pred() throws Exception {
        Tree tree = Env.tree("lr1/prec.g");
        //tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lr";
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        tree.options.packageName = "lr";
        Lr1Generator generator = new Lr1Generator(tree);
        generator.merge = true;
        generator.generate();
        generator.checkAll();
        dots(generator, tree.file.getName());
        //generator.checkAll();
        /*CodeGen codeGen = new CodeGen(generator, false);
        codeGen.gen();*/
    }

    @Test
    public void real() throws Exception {
        //Tree tree = Env.tree("lr1/calc3.g");
        Tree tree = Env.tree("lr1/calc.g");
        tree.options.packageName = "lr";
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lr";
        Lr1Generator generator = new Lr1Generator(tree);
        generator.generate();
        CodeGen codeGen = new CodeGen(generator, "lalr");
        codeGen.gen();
    }

    @Test
    public void assoc() throws Exception {
        Tree tree = Env.tree("lr1/assoc.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Lr1Generator generator = new Lr1Generator(tree);
        generator.merge = false;
        generator.generate();
        //generator.merge();
        generator.checkAll();
        dots(generator, tree.file.getName());
    }

    @Test
    public void lookahead() throws Exception {
        Tree tree = Env.tree("lr1/la.g");
        //Tree tree = Env.tree("lr1/prec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Lr1Generator generator = new Lr1Generator(tree);
        generator.generate();
        dots(generator, tree.file.getName());
    }

    @Test
    @Ignore
    public void stateCode() throws Exception {
        //Tree tree = Env.tree("lr1/calc3.g");
        //Tree tree = Env.tree("lr1/assoc.g");
        //Tree tree = Env.tree("lr1/prec2.g");
        //Tree tree = Env.tree("lr1/pred.g");
        //Tree tree = Env.tree("lr1/prec3.g");
        Tree tree = Env.tree("lr1/calc.g");
        //tree.options.outDir = Env.dotDir().getAbsolutePath();
        tree.options.packageName = "lr";
        LrDFA.debugTransition = true;
        Lr1Generator dfaGen = new Lr1Generator(tree);
        dfaGen.merge = true;
        dfaGen.generate();
        dfaGen.checkAll();
        //dfaGen.merge();
        dfaGen.genGoto();
        dots(dfaGen, tree.file.getName());
        LexerGenerator lexerGenerator = new LexerGenerator(tree);
        lexerGenerator.generate();
        StateCodeGen.debugState = true;
        StateCodeGen.debugReduce = true;
        StateCodeGen gen = new StateCodeGen(dfaGen.table, dfaGen, lexerGenerator.idMap);
        gen.gen();

        AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();
    }

    @Test
    @Ignore
    public void stateCode2() throws Exception {
        //Tree tree = Env.tree("lr1/calc3.g");
        //Tree tree = Env.tree("lr1/assoc.g");
        //Tree tree = Env.tree("lr1/prec2.g");
        //Tree tree = Env.tree("lr1/pred.g");
        //Tree tree = Env.tree("lr1/prec3.g");
        Tree tree = Env.tree("lr1/calc.g");
        //tree.options.outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lr";
        tree.options.packageName = "lr";
        LrDFA.debugTransition = true;
        Lr1Generator dfaGen = new Lr1Generator(tree);
        dfaGen.merge = true;
        dfaGen.generate();
        dfaGen.checkAll();
        dfaGen.genGoto();
        dots(dfaGen, tree.file.getName());
        LexerGenerator lexerGenerator = new LexerGenerator(tree);
        lexerGenerator.generate();
        StateCodeGen2.debugState = true;
        StateCodeGen2.debugReduce = true;
        StateCodeGen2 gen = new StateCodeGen2(dfaGen.table, dfaGen, lexerGenerator.idMap);
        gen.gen();

        /*AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();*/
    }

    @Test
    public void astBuilder() throws Exception {
        Tree tree = Env.tree("lr1/calc.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();
    }

    @Test
    public void rec() throws Exception {
        Tree tree = Env.tree("lr1/rec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Lr1Generator dfaGen = new Lr1Generator(tree);
        //dfaGen.merge = true;
        dfaGen.generate();
        dfaGen.checkAll();
        dots(dfaGen, tree.file.getName());
    }

    @Test
    public void lalrTester() throws Exception {
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
    }

    @Test
    @Ignore
    public void prec() throws Exception {
        //todo la not merged
        LrTester.check(Env.tree("lr1/rec.g"), "abc", "abd", "ababc");
    }
}
