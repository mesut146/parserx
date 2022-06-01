package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.ll.AstGen;
import mesut.parserx.nodes.Tree;

import java.io.IOException;

public class LLDFAGen {

    public static void gen(Tree tree, String target) throws IOException {
        tree.options.useSimple = false;
        AstGen.gen(tree, target);
        if (target.equals("java")) {
            new JavaGen(tree).gen();
        }

        //LexerGenerator.gen(tree, target);
    }
}
