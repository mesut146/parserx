package mesut.parserx.gen.ll;

import mesut.parserx.gen.targets.CppAstGen;
import mesut.parserx.gen.targets.JavaAst;
import mesut.parserx.gen.targets.JavaAstGen;
import mesut.parserx.nodes.Tree;

import java.io.IOException;

//generate ast file and astinfo per node
public class AstGen {

    public static void gen(Tree tree, String target) throws IOException {
        if (target.equals("java")) {
            //new JavaAstGen(tree).genAst();
            new JavaAst(tree).genAst();
        }
        else if (target.equals("cpp")) {
            new CppAstGen(tree).genAst();
        }
    }
}
