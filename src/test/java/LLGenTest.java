import mesut.parserx.gen.ll.LLGen;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class LLGenTest {

    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("ll.g"));
        LLGen gen = new LLGen(tree);
        gen.gen();
    }
}
