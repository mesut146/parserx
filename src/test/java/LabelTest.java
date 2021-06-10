import common.Env;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class LabelTest {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("labeled.g"));
        System.out.println(tree);
    }
}
