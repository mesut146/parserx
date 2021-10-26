package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.transform.Epsilons;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorLoop;
import mesut.parserx.gen.transform.Recursion;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class FactorTest {

    @Test
    public void helper() {
        Tree tree = Utils.makeTokenLessTree("A: (a | b y)* c | (a | d)+ e | c c;", false);
        RuleDecl decl = tree.rules.get(0);
        System.out.println(Helper.firstMap(decl.rhs, tree));
    }

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
        Factor factor = new Factor(tree);
        Factor.keepFactor = false;
        factor.factorize();
        System.out.println(tree);
    }

    @Test
    @Ignore
    public void loopAll() throws Exception {
        Tree tree = Env.tree("factor/loop4.g");
        FactorLoop factorLoop = new FactorLoop(tree);
        factorLoop.factorize();
        tree.printRules();
    }

    @Test
    @Ignore
    public void loopJava() throws Exception {
        //Tree tree = Env.tree("java/parser-jls-eps.g");
        //Tree tree = Env.tree("java/parser-jls-rec.g");
        Tree tree = Env.tree("java/parser-jls.g");
        //Tree tree = Env.tree("java/factor-eps.g");
        Recursion.debug = true;
        new Recursion(tree).all();
        //EpsilonTrimmer2.trim(tree);
        tree.printRules();

        //FactorLoop.debug = true;
        /*FactorLoop factorLoop = new FactorLoop(tree);
        factorLoop.factorize();
        tree.printRules();*/
    }

    @Test
    @Ignore
    public void loop() throws Exception {
        Tree tree = Env.tree("factor/loop.g");
        Node node = tree.getRule("A").rhs;
        FactorLoop factorLoop = new FactorLoop(tree);
        factorLoop.factorize();
        //node = new Regex(new Name("B"), "*");
       /* Factor.PullInfo info = factorLoop.pull(node, new Regex(new Name("a", true), "+"));
        System.out.println("one: " + info.one);
        System.out.println("zero: " + info.zero);*/
        tree.printRules();
    }

    @Test
    public void loopCode() throws IOException {
        Tree tree = Env.tree("factor/loop.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RecDescent recDescent = new RecDescent(tree);
        recDescent.gen();
    }
}
