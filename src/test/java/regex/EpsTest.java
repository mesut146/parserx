package regex;

import mesut.parserx.gen.EpsilonTrimmer;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

import java.io.File;

public class EpsTest {
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
