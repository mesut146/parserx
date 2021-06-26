package regex;

import common.Env;
import mesut.parserx.gen.EpsilonTrimmer;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class EpsTest {
    @Test
    public void test() throws Exception {
        Or.newLine = false;
        Tree tree = Tree.makeTree(Env.getResFile("eps.g"));
        System.out.println(new EpsilonTrimmer(tree).trim());
    }
}
