package mesut.parserx.gen.ll;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.*;
import mesut.parserx.gen.targets.CppRecDescent;
import mesut.parserx.gen.targets.JavaMulti;
import mesut.parserx.gen.transform.*;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Regex;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

// ll(1) recursive descent parser generator
public class Multi {
    public static boolean debug = false;
    public static int loopLimit = 10;
    public static String tokens = "Tokens";
    public Options options;
    Tree tree;
    String target;

    public Multi(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public static Multi gen(Tree tree, String target) throws IOException {
        Multi recDescent = new Multi(tree);
        recDescent.target = target;
        recDescent.gen();
        return recDescent;
    }

    public static boolean isSimple(Node node) {
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return regex.node.isName();
        }
        return node.isName();
    }

    public void gen() throws IOException {
        prepare();

        if (target.equals("java")) {
            new JavaMulti(tree).gen();
        }
        else if (target.equals("cpp")) {
            new CppRecDescent(tree).gen();
        }
    }

    private void prepare() throws IOException {
        Simplify.all(tree);
        tree = EbnfToBnf.combineOr(tree);
        AstGen.gen(tree, target);

        PrecedenceHandler.handle(tree);

        File out = new File(options.outDir, Utils.newName(tree.file.getName(), "-final.g"));
        Node.printVarName = false;
        Utils.write(tree.toString(), out);

        LexerGenerator.gen(tree, target);
    }


}
