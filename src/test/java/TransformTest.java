import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.PrecedenceHelper;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class TransformTest {


    @Test
    public void ebnf() throws Exception {
        Tree tree = Env.tree("ebnf.g");
        EbnfToBnf.transform(tree).printRules();
    }

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
        Helper.revert(tree);
        System.out.println(NodeList.join(tree.rules, "\n"));
        /*tree = LeftRecursive.transform(tree);
        System.out.println("-------------");
        System.out.println(NodeList.join(tree.rules, "\n"));*/
    }
}
