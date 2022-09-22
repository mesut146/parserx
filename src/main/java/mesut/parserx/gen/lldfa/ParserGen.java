package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.ll.AstGen;
import mesut.parserx.gen.ll.Normalizer;
import mesut.parserx.nodes.Tree;

import java.io.IOException;

public class ParserGen {

    public static void gen(Tree tree, Lang target) throws IOException {
        tree.prepare();
        new Normalizer(tree).normalize();
        AstGen.gen(tree, target);
        if (target == Lang.JAVA) {
            new JavaGen(tree).gen();
        }
        else {
            throw new RuntimeException("not yet");
        }
        LexerGenerator.gen(tree, target);
    }
}
