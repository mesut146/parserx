import gen.LrGenerator;
import nodes.Tree;
import org.junit.Test;

public class Lr0Test {


    @Test
    public void test() {
        Tree tree = Tree.makeTree(Env.getCalc());
        LrGenerator generator = new LrGenerator(null, null, tree);
        generator.generate();
    }
}
