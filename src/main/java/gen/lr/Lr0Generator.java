package gen.lr;

import gen.parser.BnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.Tree;

import java.io.PrintWriter;
import java.util.*;

// lr(0)
public class Lr0Generator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;
    List<LrTransition> transitions;
    List<Lr0ItemSet> itemSets = new ArrayList<>();
    public PrintWriter dotWriter;
    Map<Lr0ItemSet, Integer> idMap = new HashMap<>();
    int lastId = -1;

    public Lr0Generator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        check();
    }



    private void check() {
        PrepareTree.checkReferences(tree);
        BnfTransformer.rhsSequence = true;
        BnfTransformer transformer = new BnfTransformer(tree);
        tree = transformer.transform();
        PrepareTree.checkReferences(tree);
    }
}
