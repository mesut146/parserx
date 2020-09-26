import gen.ClosureHelper;
import gen.EbnfTransformer;
import gen.PrepareTree;
import nodes.Tree;
import org.junit.Test;

import java.io.File;

public class ParserGenTest {

    @Test
    public void test() {
        Tree tree = Tree.makeTree(Env.getJavaLexer());
        PrepareTree.checkReferences(tree);
    }

    @Test
    public void recursiveTest() throws Exception {
        String input = "1+2*3.1415/(66-33)";
        File grammar = Env.getCalc();


        //LexerGenTest.generateLexer(grammar);
        //LexerGenTest.tokenizerTest(new StringReader(input));

        Tree tree = Tree.makeTree(grammar);
        PrepareTree.checkReferences(tree);
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
        System.out.println(tree);

        new ClosureHelper(tree).all();
    }


}
