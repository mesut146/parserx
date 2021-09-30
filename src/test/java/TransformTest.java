import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.PrecedenceHelper;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class TransformTest {


    @Test
    public void ebnf() throws Exception {
        Tree tree = Env.tree("ebnf.g");
        EbnfToBnf.transform(tree).printRules();
    }

    @Test
    public void test() throws Exception {
        Tree tree = Env.tree("java/parser-jls.g");
        //System.out.println(new EbnfToBnf(tree).transform());
    }

    @Test
    public void pred() throws Exception {
        Tree tree = Env.tree("pred.g");
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
