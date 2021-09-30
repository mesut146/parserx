package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NfaReader;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.parser.Lexer;
import mesut.parserx.parser.AstBuilder;
import mesut.parserx.parser.Parser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class NfaTest {

    static Node makeRegex(String regex) throws IOException {
        return new AstBuilder().visitRhs(new Parser(new Lexer(new StringReader(regex))).rhs());
    }

    static Tree makeTree(String regex) throws IOException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("test", makeRegex(regex)));
        return tree;
    }

    @Test
    public void splitRanges() throws IOException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("hex", makeRegex("[a-f]")));
        tree.addToken(new TokenDecl("a", makeRegex("[b-z]")));
        tree.makeNFA();
    }

    @Ignore
    @Test
    public void reader() throws Exception {
        NFA nfa = NfaReader.read(Env.getResFile("fsm/in.nfa"));
        nfa.dump();
    }

    @Ignore
    @Test
    public void test() throws IOException {
        String regex = "(\"a\" \"b\"?)+";
        Tree tree = makeTree(regex);

        NFA nfa = tree.makeNFA();
        NFA dfa = nfa.dfa();

        nfa.dot(new FileWriter(Env.dotFile("test-nfa.dot")));
        dfa.dot(new FileWriter(Env.dotFile("test-dfa.dot")));
    }
}
