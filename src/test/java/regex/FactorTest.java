package regex;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.ll.RDParserGen;
import mesut.parserx.gen.transform.Epsilons;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorLoop;
import mesut.parserx.gen.transform.Recursion;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;
import parser.DescTester;

import java.io.IOException;

public class FactorTest {

    @Test
    public void eps() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        Tree tree = Env.tree("eps.g");
        System.out.println(Epsilons.trim(tree.getRule("C").rhs, tree));
        System.out.println(tree);
    }

    @Test
    public void pull() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("factor/single.g");
        Factor factor = new Factor(tree);

        Factor.PullInfo info = factor.pull(new Name("A", false), new Name("a", true));
        System.out.printf("0=%s\n1=%s\n", info.zero, info.one);
        System.out.println(tree);
    }

    @Test
    public void all() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("factor/single.g");
        //Tree tree = Env.tree("rec/leftRec2.g");
        Factor.keepFactor = false;
        new FactorLoop(tree, null).factorize();
        System.out.println(tree);
    }

    @Test
    @Ignore
    public void loopAll() throws Exception {
        Tree tree = Env.tree("factor/loop4.g");
        FactorLoop factorLoop = new FactorLoop(tree, null);
        factorLoop.factorize();
        tree.printRules();
    }

    @Test
    @Ignore
    public void loopJava() throws Exception {
        //Tree tree = Env.tree("java/parser-jls-eps.g");
        //Tree tree = Env.tree("java/parser-jls-rec.g");
        Tree tree = Env.tree("java/parser-jls.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //Tree tree = Env.tree("java/factor-eps.g");
        Factor.debug = true;
        Recursion.debug = true;
        RDParserGen.gen(tree, Lang.JAVA);
    }

    @Test
    @Ignore
    public void loop() throws Exception {
        Tree tree = Env.tree("factor/loop.g");
        FactorLoop factorLoop = new FactorLoop(tree, null);
        factorLoop.factorize();
        //node = new Regex(new Name("B"), "*");
       /* Factor.PullInfo info = factorLoop.pull(node, new Regex(new Name("a", true), "+"));
        System.out.println("one: " + info.one);
        System.out.println("zero: " + info.zero);*/
        tree.printRules();
    }


    @Test
    public void loopCode() throws IOException {
        Factor.debug = true;
        Tree tree = Env.tree("factor/loop.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RDParserGen.gen(tree,Lang.JAVA);
    }

    @Test
    public void loopReal() throws Exception {
        //DescTester.check(Env.tree("factor/loop.g"), "A1", "b", "c", "ab", "ac", "aaaab");
        //DescTester.check(Env.tree("factor/loop.g"), "B", "b", "dc", "ec", "aaab", "aaadc");
        //DescTester.check(Env.tree("factor/loop.g"), "A", "b", "c", "ad", "ab", "ac", "aaab", "aaac");
        //DescTester.check(Env.tree("factor/loop.g"), "A", "b", "dc", "aab", "aadc", "aaec", "af");
        DescTester.check(Env.tree("factor/loop.g"), "E", "b", "c", "aaab", "aaac");
    }
}
