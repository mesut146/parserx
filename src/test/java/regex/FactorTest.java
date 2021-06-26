package regex;

import common.Env;
import mesut.parserx.gen.Factor;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class FactorTest {
    @Test
    public void pull() throws Exception {
        Or.newLine = false;
        Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        Factor factor = new Factor(tree);
        Factor.PullInfo info = factor.pull(new Name("E", false), new Name("a", true));
        System.out.printf("E0=%s\nE1=%s\n", info.zero, info.one);
    }

    @Test
    public void all() throws Exception {
        Or.newLine = false;
        Tree tree = Tree.makeTree(Env.getResFile("factor.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("rec/leftRec2.g"));
        Factor factor = new Factor(tree);
        factor.handle();
        factor.handle();
        System.out.println(tree);
    }
}
