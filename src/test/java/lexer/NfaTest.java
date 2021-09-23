package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NfaReader;
import mesut.parserx.grammar.ParseException;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.parser.Lexer;
import mesut.parserx.parser.MyVisitor;
import mesut.parserx.parser.Parser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class NfaTest {

    static Node makeRegex(String regex) throws IOException {
        return new MyVisitor().visitRhs(new Parser(new Lexer(new StringReader(regex))).rhs());
    }

    static Tree makeTree(String regex) throws IOException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("test", makeRegex(regex)));
        return tree;
    }

    public static NFA makeNFA(File grammar) {
        Tree tree = Tree.makeTree(grammar);
        NFA nfa = tree.makeNFA();
        //System.out.println("total nfa states=" + nfa.lastState);
        //nfa.dump("");
        return nfa;
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
        //File file = Env.getResFile("fsm/comment.nfa");
        File file = new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/nfa2dfa/in.nfa");
        NFA nfa = NfaReader.read(file);
        nfa.dump();
    }

    @Ignore
    @Test
    public void test() throws ParseException, IOException {
        String regex = "(\"a\" \"b\"?)+";
        Tree tree = makeTree(regex);

        NFA nfa = tree.makeNFA();
        NFA dfa = nfa.dfa();

        nfa.dot(new FileWriter(Env.testRes + "/test-nfa.dot"));
        dfa.dot(new FileWriter(Env.testRes + "/test-dfa.dot"));
    }
}
