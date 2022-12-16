package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.LeftRecursive;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class LeftRecursionTest {

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
}
