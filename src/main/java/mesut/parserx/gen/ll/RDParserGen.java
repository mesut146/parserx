package mesut.parserx.gen.ll;

import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.targets.CppRecDescent;
import mesut.parserx.gen.targets.JavaRecDescent;
import mesut.parserx.gen.transform.*;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;

// ll(1) recursive descent parser generator
public class RDParserGen {
    public static boolean debug = false;
    public static int loopLimit = 10;
    public static String tokens = "Tokens";
    public Options options;
    Tree tree;
    String target;

    public RDParserGen(Tree tree) {
        this.tree = tree;
        this.options = tree.options;
    }

    public static RDParserGen gen(Tree tree, String target) throws IOException {
        var recDescent = new RDParserGen(tree);
        recDescent.target = target;
        recDescent.gen();
        return recDescent;
    }

    public void gen() throws IOException {
        prepare();

        if (target.equals("java")) {
            new JavaRecDescent(tree).gen();
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

        Recursion recursion = new Recursion(tree);
        recursion.all();

        FactorLoop factorLoop = new FactorLoop(tree, recursion.factor);
        factorLoop.factorize();

        GreedyNormalizer greedyNormalizer = new GreedyNormalizer(tree, factorLoop);
        greedyNormalizer.normalize();

        //Optimizer.optimize(tree);

        File out = new File(options.outDir, Utils.newName(tree.file.getName(), "-final.g"));
        Node.printVarName = false;
        Utils.write(tree.toString(), out);

        LexerGenerator.gen(tree, target);
    }


}
