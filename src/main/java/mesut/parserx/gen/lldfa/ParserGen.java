package mesut.parserx.gen.lldfa;

import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.ast.AstGen;
import mesut.parserx.nodes.Tree;

import java.io.IOException;

public class ParserGen {

    public static void gen(Tree tree, Lang target) throws IOException {
        tree.prepare();
        LexerGenerator.gen(tree, target);
        new Normalizer(tree).normalize();
        AstGen.gen(tree, target);
        if (target == Lang.JAVA) {
            new JavaGen(tree).gen();
        }
        else {
            throw new RuntimeException("not yet");
        }
    }

    public static void genCC(Tree tree, Lang target, boolean regex) throws IOException {
        tree.prepare();
        LexerGenerator.gen(tree, target);
        new Normalizer(tree).normalize();
        AstGen.gen(tree, target);
        if (target == Lang.JAVA) {
            if (regex) {
                new CcGenJava(tree).gen();
            }
            else {
                new CcStateGenJava(tree).gen();
            }
        }
        else {
            throw new RuntimeException("not yet");
        }
    }
}
