import gen.PrepareTree;
import nodes.Tree;
import org.junit.Test;

public class ParserGenTest {

    @Test
    public void test() {
        Tree tree = Tree.makeTree(LexerGenTest.getGrammar());
        PrepareTree.prepare(tree);
    }
}
