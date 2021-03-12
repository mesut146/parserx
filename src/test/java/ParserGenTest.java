import gen.parser.ClosureHelper;
import gen.parser.BnfTransformer;
import gen.PrepareTree;
import nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class ParserGenTest {

    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getJavaLexer());
        PrepareTree.checkReferences(tree);
    }

    @Test
    @Ignore
    public void recursiveTest() throws Exception {
        String input = "1+2*3.1415/(66-33)";
        File grammar = Env.getCalc();


        //LexerGenTest.generateLexer(grammar);
        //LexerGenTest.tokenizerTest(new StringReader(input));

        Tree tree = Tree.makeTree(grammar);
        PrepareTree.checkReferences(tree);
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);
        System.out.println(tree);

        new ClosureHelper(tree).all();
    }


}
