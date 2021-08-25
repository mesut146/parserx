package regex;

import common.Env;
import mesut.parserx.gen.Epsilons;
import mesut.parserx.gen.Factor;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class FactorTest {

    @Test
    public void eps() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        Tree tree = Tree.makeTree(Env.getResFile("eps.g"));
        System.out.println(new Epsilons(tree).trim(tree.getRule("C").rhs));
        System.out.println(tree);
    }

    @Test
    public void pull() throws Exception {
        Or.newLine = false;
        Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        Factor factor = new Factor(tree);

        Factor.PullInfo info = factor.pull(new Name("E", false), new Name("A", false));
        System.out.printf("E0=%s\nE1=%s\n", info.zero, info.one);
        System.out.println(tree);
    }

    @Test
    public void all() throws Exception {
        Or.newLine = false;
        Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("rec/leftRec2.g"));
        Factor factor = new Factor(tree);
        factor.handle();
        System.out.println(tree);
    }

    @Test
    public void java() throws Exception {
        Tree tree = Tree.makeTree(Env.getFile2("java/parser-jls.g"));
        Factor factor = new Factor(tree);
        factor.handle();
        System.out.println(tree);
    }
}
