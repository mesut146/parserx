package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EpsilonTrimmer;
import mesut.parserx.gen.transform.EpsilonTrimmer2;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

public class EpsTest {

    @Test
    @Ignore
    public void jls() throws Exception {
        Tree tree = Env.tree("java/parser-jls.g");
        //tree = EbnfToBnf.transform(tree);
        //tree = EbnfToBnf.combineOr(tree);
        tree = EpsilonTrimmer2.trim(tree);
        tree.printRules();
        //Helper.revert(tree);
        //Simplify.all(tree);
    }

    @Test
    public void trim() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("eps.g");
        tree = new EpsilonTrimmer2(tree).trim();
        Helper.revert(tree);
        System.out.println(tree);
    }
}
