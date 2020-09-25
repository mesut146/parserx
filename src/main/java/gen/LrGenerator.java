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
        RuleDecl start = new RuleDecl("s'", tree.start);
        LrItem item = new LrItem(start, 0);
    }
}
