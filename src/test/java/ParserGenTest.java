import gen.LexerGenerator;
import gen.ll.RecGenerator;
import gen.parser.ClosureHelper;
import gen.BnfTransformer;
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
        Tree tree = Tree.makeTree(Env.getResFile("calc.g"));
        RecGenerator generator = new RecGenerator(tree);
        generator.className = "Parser";
        generator.lexerClass = "Lexer";
        generator.outDir = Env.dotDir().getAbsolutePath();
        generator.generate();
    }


}
