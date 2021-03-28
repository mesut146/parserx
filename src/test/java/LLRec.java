import mesut.parserx.gen.ll.RecGenerator;
import mesut.parserx.nodes.Tree;
import org.junit.Test;

public class LLRec {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("javaParser.g"));
        RecGenerator gen = new RecGenerator(tree);
        gen.generate();
    }
}
