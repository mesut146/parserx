import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.EpsilonTrimmer;
import mesut.parserx.gen.transform.PrecedenceHelper;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

public class TransformTest {


    @Test
    public void ebnf() throws Exception {
        Tree tree = Env.tree("ebnf.g");
        tree = EbnfToBnf.transform(tree);
        EpsilonTrimmer.preserveNames = true;
        EpsilonTrimmer.trim(tree).printRules();
        //tree.printRules();
    }

    @Test
    @Ignore
    public void jls() throws Exception {
        Tree tree = Env.tree("java/parser-jls.g");
        //tree = EbnfToBnf.transform(tree);
        //tree = EbnfToBnf.combineOr(tree);
        tree = EpsilonTrimmer.trim(tree);
        tree.printRules();
        //Helper.revert(tree);
        //Simplify.all(tree);
    }

    @Test
    public void trim() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("eps.g");
        tree = EpsilonTrimmer.trim(tree);
        tree.printRules();
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
