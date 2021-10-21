package parser;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.LLDfaBuilder;
import mesut.parserx.gen.ll.LLDfaParserGen;
import mesut.parserx.gen.ll.LLDfaRegex;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LLDfaTest {

    void dots(NFA nfa, File f) throws IOException {
        File file = Env.dotFile(Utils.newName(f.getName(), ".dot"));
        nfa.dot(new FileWriter(file));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    @Test
    public void dfa() throws IOException {
        //Tree tree = Env.tree("lldfa/a.g");
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.build();
        NFA nfa = builder.dfa;
        NFA dfa = nfa.dfa();
        dfa = Minimization.optimize(dfa);
        dots(Minimization.combineAlphabet(dfa), tree.file);
    }

    @Test
    public void llregex() throws IOException {
        Tree tree = Env.tree("lldfa/a.g");
        LLDfaRegex regex = new LLDfaRegex(tree);
        System.out.println(regex.makeRegex(tree.getRule("A")));
        //c | a a* c | c b b* | a a* c b b*
        dots(regex.dfa, tree.file);
    }

    @Test
    public void real() throws IOException {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        //tree.options.outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/parser/lldfa";
        tree.options.packageName = "parser.lldfa";
        tree.options.lexerClass = "Lexer1";
        tree.options.parserClass = "Parser1";
        tree.options.tokenClass = "Token1";
        LLDfaParserGen gen = new LLDfaParserGen(tree);
        gen.gen();

    }
}
