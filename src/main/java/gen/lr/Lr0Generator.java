package gen.lr;

import gen.EbnfTransformer;
import gen.IndentWriter;
import gen.LexerGenerator;
import gen.PrepareTree;
import nodes.NameNode;
import nodes.Node;
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
        EbnfTransformer.rhsSequence = true;
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
    }
}
