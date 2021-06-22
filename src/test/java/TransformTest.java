import common.Env;
import mesut.parserx.gen.EbnfToBnf;
import mesut.parserx.gen.PrecedenceHelper;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class TransformTest {
    @Test
    public void test() throws Exception {
        File file = Env.getResFile("java/parser-jls.g");
        //File file = Env.getResFile("bnf.g");
        Tree tree = Tree.makeTree(file);
        //System.out.println(new EbnfToBnf(tree).transform());
    }

    @Test
    public void pred() throws Exception {
        File file = Env.getResFile("pred.g");
        Tree tree = Tree.makeTree(file);
        //System.out.println(tree);
        PrecedenceHelper helper = new PrecedenceHelper(tree);
        tree = helper.transform();
        PrepareTree.revert(tree);
        System.out.println(NodeList.join(tree.rules, "\n"));
        /*tree = LeftRecursive.transform(tree);
        System.out.println("-------------");
        System.out.println(NodeList.join(tree.rules, "\n"));*/
    }
}
