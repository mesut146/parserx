import gen.ll.RecGenerator;
import nodes.Tree;
import org.junit.Test;

public class LLRec {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("calc.g"));
        RecGenerator gen = new RecGenerator(tree);
        gen.generate();
    }
}
