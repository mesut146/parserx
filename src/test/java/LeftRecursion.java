import mesut.parserx.gen.LeftRecursive;
import mesut.parserx.nodes.RuleDecl;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class LeftRecursion {

    @Test
    public void remove() throws Exception {
        File file = Env.getResFile("rec/leftRec3.g");
        Tree tree = Tree.makeTree(file);
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        RuleDecl rule = tree.getRule("E");
        leftRecursive.process();
        System.out.println(tree);
    }

    @Test
    public void split() throws Exception {
        File file = Env.getResFile("rec/leftRec3.g");
        Tree tree = Tree.makeTree(file);
        RuleDecl rule = tree.getRule("E");
        LeftRecursive left = new LeftRecursive(tree);
        LeftRecursive.SplitInfo info = left.split(rule.rhs, rule.ref());
        System.out.println("zero = " + info.zero);
        System.out.println("one = " + info.one);
    }
}
