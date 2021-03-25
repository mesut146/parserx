import gen.parser.BnfTransformer;
import gen.parser.EbnfTransformer;
import nodes.Tree;
import org.junit.Test;

import java.io.File;

public class TransformTest {
    @Test
    public void test() throws Exception {
        //File file = Env.getResFile("javaParser.g");
        File file = Env.getResFile("bnf.g");
        Tree tree = Tree.makeTree(file);
        System.out.println(new BnfTransformer(tree).transform());
    }
}
