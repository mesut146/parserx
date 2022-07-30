package parser;

import common.Env;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.lr.*;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

import static parser.LrTest.dots;

public class LrStateGenTest {
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
        LrDFAGen dfaGen = new LrDFAGen(tree, "lalr");
        dfaGen.generate();
        dfaGen.checkAndReport();
        dfaGen.genGoto();
        dots(dfaGen, tree.file.getName());
        LexerGenerator lexerGenerator = LexerGenerator.gen(tree, "java");
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
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFA.debugTransition = true;
        LrDFAGen dfaGen = new LrDFAGen(tree, "lalr");
        dfaGen.generate();
        dfaGen.checkAndReport();
        dfaGen.genGoto();
        dots(dfaGen, tree.file.getName());
        LexerGenerator lexerGenerator = LexerGenerator.gen(tree, "java");
        StateCodeGen2.debugState = true;
        StateCodeGen2.debugReduce = true;
        StateCodeGen2 gen = new StateCodeGen2(dfaGen.table, dfaGen, lexerGenerator.idMap);
        gen.gen();

        /*AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();*/
    }
}
