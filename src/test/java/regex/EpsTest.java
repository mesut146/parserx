package regex;

import common.Env;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.EpsilonTrimmer;
import mesut.parserx.gen.transform.EpsilonTrimmer2;
import mesut.parserx.gen.transform.Simplify;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class EpsTest {

    @Test
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
    public void test() throws Exception {
        Or.newLine = false;
        //Tree tree = Tree.makeTree(Env.getResFile("eps.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/dots/recc.g"));
        tree = new EpsilonTrimmer(tree).trim();
        Helper.revert(tree);
        System.out.println(tree);
    }
}
