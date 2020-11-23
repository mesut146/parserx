import dfa.DFA;
import dfa.NFA;
import dfa.RegexBuilder;
import grammar.GParser;
import grammar.ParseException;
import nodes.Node;
import nodes.TokenDecl;
import nodes.Tree;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;

public class NfaTest {

    @Test
    public void splitRanges() throws ParseException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("hex", makeRegex("[a-f]")));
        tree.addToken(new TokenDecl("a", makeRegex("[b-z]")));
        tree.makeNFA();
    }

    static Node makeRegex(String regex) throws ParseException {
        return new GParser(new StringReader(regex)).rhs();
    }

    static Tree makeTree(String regex) throws ParseException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("test", makeRegex(regex)));
        return tree;
    }

    @Test
    public void comment() throws ParseException {
        //String regex = "\"/*\" \"x\"* \"*\" (\"yx*\")* \"/\"";
        String regex = "\"/*\" (\"x\" | \"*y\")* \"*/\"";
        Tree tree = makeTree(regex);
        NFA nfa = tree.makeNFA();
        DFA dfa = nfa.dfa();

        nfa.dot(Env.testRes + "/test-nfa.dot");
        dfa.dot(Env.testRes + "/test-dfa.dot");
    }

    @Test
    public void reader() throws Exception {
        File file = Env.getResFile("fsm/test2.nfa");
        NFA nfa = NfaReader.read(file);
        //nfa.dump(null);
        System.out.println(new RegexBuilder(nfa).buildRegex());
    }

    @Test
    public void test() throws ParseException {
        String regex = "(\"a\" \"b\"?)+";
        Tree tree = makeTree(regex);

        NFA nfa = tree.makeNFA();
        DFA dfa = nfa.dfa();

        nfa.dot(Env.testRes + "/test-nfa.dot");
        dfa.dot(Env.testRes + "/test-dfa.dot");
    }

    static NFA makeNFA(File grammar) {
        Tree tree = Tree.makeTree(grammar);
        NFA nfa = tree.makeNFA();
        System.out.println("total nfa states=" + nfa.lastState);
        //nfa.dump("");
        return nfa;
    }
}
