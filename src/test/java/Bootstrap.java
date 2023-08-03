import common.Env;
import lexer.RealTest;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Log;
import org.junit.Test;
import parser.Builder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Bootstrap {

    @Test
    public void nfaReader() throws Exception {
        Log.curLevel = Level.ALL;
        Tree tree = Tree.makeTree(new File("./src/main/grammar/nfaReader.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //tree.options.packageName="mesut.parserx.dfa.parser";
        //ParserGen.gen(tree, Lang.JAVA);
        Builder.tree(tree).rule("nfa")
                .file(Env.getResFile("fsm/a.nfa").getAbsolutePath())
                .file(Env.getResFile("fsm/dfa-min.dfa").getAbsolutePath())
                .dump()
                .check();
    }

    @Test
    public void lexerGen() throws Exception {
        var grammar = new File("./src/main/grammar/parserx.g");
        Tree tree = Tree.makeTree(grammar);
        tree.options.dump = true;
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //RealTest.check(tree, true, grammar.getAbsolutePath());
        RealTest.check(tree, true, Env.getResFile("lexer/member.g").getAbsolutePath());
    }

    @Test
    public void generateCC() throws IOException {
        Tree tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        tree.options.packageName = "mesut.parserx.parser";
        ParserGen.genCC(tree, Lang.JAVA);
    }

    @Test
    public void itself() throws Exception {
        //Item.printLa = false;
        File grammar = new File("./src/main/grammar/parserx.g");
        Builder.tree(Tree.makeTree(grammar)).rule("tree")
                .dump()
                .file(grammar.getAbsolutePath())
                .file(Env.getResFile("lexer/action.g").getAbsolutePath())
                .file(Env.getResFile("lexer/member.g").getAbsolutePath())
                .file(Env.getResFile("lexer/xml-mode.g").getAbsolutePath())
                .checkTokens();
    }
}
