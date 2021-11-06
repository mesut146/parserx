package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.LeftRecursive;
import mesut.parserx.gen.transform.Recursion;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import org.junit.Test;
import parser.DescTester;

import java.io.IOException;

public class LeftRecursionTest {

    @Test
    public void withAst() throws Exception {
        Tree tree = Env.tree("rec/direct.g");
        new Recursion(tree).all();
        tree.printRules();
    }

    @Test
    public void withAst2() throws Exception {
        Tree tree = Env.tree("rec/indirect.g");
        new Recursion(tree).all();
        tree.printRules();
    }

    @Test
    public void remove() throws Exception {
        Tree tree = Env.tree("rec/leftRec2.g");
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        leftRecursive.process();
        Helper.revert(tree);
        EbnfToBnf.expand_or = false;
        tree = EbnfToBnf.transform(tree);
        System.out.println(tree);
    }

    @Test
    public void split() throws Exception {
        Tree tree = Env.tree("rec/leftRec.g");
        RuleDecl rule = tree.getRule("A");
        LeftRecursive left = new LeftRecursive(tree);
        LeftRecursive.SplitInfo info = left.split(rule.rhs, rule.ref);
        System.out.println("zero = " + info.zero);
        System.out.println("one = " + info.one);
    }

    @Test
    public void direct() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("rec/direct.g");
        tree = LeftRecursive.transform(tree);
        tree.printRules();
    }

    @Test
    public void rec() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("rec/cyc2.g");
        tree = LeftRecursive.transform(tree);
        tree.printRules();
    }

    @Test
    public void indirect3() throws IOException {
        Tree tree = Env.tree("rec/cyc0.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        /*LeftRecursive l = new LeftRecursive(tree);
        l.normalizeIndirects();
        tree.printRules();*/
        //l.process();
        RecDescent recDescent = new RecDescent(tree);
        recDescent.gen();
    }

    @Test
    public void indirectFactor() throws IOException {
        Tree tree = Env.tree("rec/cyc0.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RecDescent recDescent = new RecDescent(tree);
        recDescent.gen();
    }

    @Test
    public void indirectReal() throws Exception {
        DescTester.check(Env.tree("rec/cyc0.g"), "A", "cab");
        //DescTester.check(Env.tree("rec/cyc0.g"), "B", "ca");
    }

    @Test
    public void sub() throws IOException {
        Tree tree = Env.tree("ll/substitude.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //l.process();
        RecDescent recDescent = new RecDescent(tree);
        recDescent.gen();
    }

    @Test
    public void name() throws IOException {
        Tree tree = Env.tree("rec/a.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RecDescent recDescent = new RecDescent(tree);
        recDescent.gen();
    }
}
