import gen.PrepareTree;
import gen.EbnfTransformer;
import nodes.Tree;
import org.junit.Test;

import java.io.File;

public class ParserGenTest {

    @Test
    public void test() {
        Tree tree = Tree.makeTree(LexerGenTest.getGrammar());
        PrepareTree.checkReferences(tree);
    }

    @Test
    public void recursiveTest() throws Exception {
        String input = "1+2*3.1415/(66-33)";
        File grammar = LexerGenTest.getCalc();


        //LexerGenTest.generateLexer(grammar);
        //LexerGenTest.tokenizerTest(new StringReader(input));

        Tree tree = Tree.makeTree(grammar);
        //PrepareTree.checkReferences(tree);
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        System.out.println(tree);

    }
}
