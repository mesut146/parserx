package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.parser.AstBuilder;
import mesut.parserx.parser.Lexer;
import mesut.parserx.parser.Parser;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

public class NfaTest {

    static Node makeRegex(String regex) throws IOException {
        return new AstBuilder().visitRhs(new Parser(new Lexer(new StringReader(regex))).rhs());
    }

    @Test
    public void splitRanges() throws IOException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("hex", makeRegex("[a-f]")));
        tree.addToken(new TokenDecl("a", makeRegex("[b-z]")));
        tree.makeNFA().dump(new PrintWriter(System.out));
    }

    @Ignore
    @Test
    public void reader() throws Exception {
        NFA nfa = NFA.read(Env.getResFile("fsm/in.nfa"));
        nfa.dump();
    }

    @Test
    public void reader2() throws IOException {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/nfaReader.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RecDescent.gen(tree, "java");
    }

    @Test
    public void lineComment() throws IOException {
        NFA nfa = NFA.read("start=0\nfinal=2\n0->1,/\n1->2,/\n2->2,[^\\n]");
        Node node = new RegexBuilder(nfa).buildRegex();
        System.out.println(node);
    }

    @Test
    public void nfa() throws IOException {
        System.out.println(RegexBuilder.from(NFA.read(new File("/home/mesut/Desktop/a.dfa"))));
    }
}
