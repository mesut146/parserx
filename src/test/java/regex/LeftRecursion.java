package regex;

import common.Env;
import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.LeftRecursive;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class LeftRecursion {

    @Test
    public void remove() throws Exception {
        File file = Env.getResFile("rec/leftRec2.g");
        Tree tree = Tree.makeTree(file);
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        leftRecursive.process();
        Helper.revert(tree);
        EbnfToBnf.expand_or = false;
        tree = EbnfToBnf.transform(tree);
        System.out.println(tree);
    }

    @Test
    public void split() throws Exception {
        File file = Env.getResFile("rec/leftRec.g");
        Tree tree = Tree.makeTree(file);
        RuleDecl rule = tree.getRule("A");
        LeftRecursive left = new LeftRecursive(tree);
        LeftRecursive.SplitInfo info = left.split(rule.rhs, rule.ref());
        System.out.println("zero = " + info.zero);
        System.out.println("one = " + info.one);
    }

    @Test
    public void direct() throws Exception {
        Or.newLine = false;
        File file = Env.getResFile("rec/direct.g");
        Tree tree = Tree.makeTree(file);
        tree = LeftRecursive.transform(tree);
        System.out.println(NodeList.join(tree.rules, "\n"));
    }


    @Test
    public void rec() throws Exception {
        Or.newLine = false;
        File file = Env.getResFile("rec/cyc2.g");
        Tree tree = Tree.makeTree(file);
        tree = LeftRecursive.transform(tree);
        System.out.println(NodeList.join(tree.rules, "\n"));
    }
}
