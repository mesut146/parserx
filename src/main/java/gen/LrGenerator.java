package gen;

import nodes.Tree;
import rule.RuleDecl;

//lr(1),lalr(1)
public class LrGenerator extends IndentWriter {
    Tree tree;
    String dir;
    LexerGenerator lexerGenerator;

    public LrGenerator(LexerGenerator lexerGenerator, String dir, Tree tree) {
        this.lexerGenerator = lexerGenerator;
        this.dir = dir;
        this.tree = tree;
    }

    public void generate() {
        check();
        //System.out.println(tree);

        RuleDecl start = new RuleDecl("s'", tree.start);
        Lr0Item first = new Lr0Item(start, 0);

        Lr0ItemSet itemSet = new Lr0ItemSet(first);
        itemSet.tree = tree;
        itemSet.closure();
        System.out.println(itemSet);
    }

    private void check() {
        PrepareTree.checkReferences(tree);
        EbnfTransformer transformer = new EbnfTransformer(tree);
        tree = transformer.transform(tree);
        PrepareTree.checkReferences(tree);
    }
}
