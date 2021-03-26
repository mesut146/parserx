import gen.EpsilonTrimmer;
import nodes.Tree;
import org.junit.Test;

public class EpsTest {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("eps.g"));
        System.out.println(new EpsilonTrimmer(tree).trim());
    }
}
