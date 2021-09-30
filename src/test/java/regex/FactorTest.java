package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.Epsilons;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.FactorLoop;
import mesut.parserx.nodes.*;
import org.junit.Ignore;
import org.junit.Test;

public class FactorTest {

    @Test
    public void factor2() {
        Tree tree = Env.makeRule("A: a b | B c | d e | f;\n" +
                "B: a x | d y | x;");
        new Factor(tree).factorize();
        System.out.println(tree);
    }

    @Test
    public void helper() {
        Tree tree = Env.makeRule("A: (a | b)* c | (a | d)+ e | c c;");
        RuleDecl decl = tree.rules.get(0);
        System.out.println(Helper.firstMap(decl.rhs, tree));
    }

    @Test
    public void eps() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        Tree tree = Env.tree("eps.g");
        System.out.println(new Epsilons(tree).trim(tree.getRule("C").rhs));
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
    public void loopAll() throws Exception {
        Tree tree = Env.tree("factor/loop4.g");
        FactorLoop factorLoop = new FactorLoop(tree);
        factorLoop.factorize();
        tree.printRules();
    }

    @Test
    @Ignore
    public void loopJava() throws Exception {
        Tree tree = Env.tree("java/parser-jls.g");
        FactorLoop factorLoop = new FactorLoop(tree);
        factorLoop.factorize();
        tree.printRules();
    }

    @Test
    public void loop() throws Exception {
        Tree tree = Env.tree("factor/loop4.g");
        Node node = tree.getRule("E").rhs;
        FactorLoop factorLoop = new FactorLoop(tree);
        //node = new Regex(new Name("B"), "*");
        Factor.PullInfo info = factorLoop.pull(node, new Name("C") {{
            isStar = true;
        }});
        System.out.println("one: " + info.one);
        System.out.println("zero: " + info.zero);
        tree.printRules();
    }
}
