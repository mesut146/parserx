package mesut.parserx.gen.ll;

import mesut.parserx.gen.Lang;
import mesut.parserx.gen.targets.CppAstGen;
import mesut.parserx.gen.targets.JavaAst;
import mesut.parserx.nodes.Tree;

import java.io.IOException;

//generate ast file and astinfo per node
public class AstGen {

    public static void gen(Tree tree, Lang target) throws IOException {
        if (target == Lang.JAVA) {
            new JavaAst(tree).genAst();
        }
        else if (target == Lang.CPP) {
            new CppAstGen(tree).genAst();
        }
    }
}
