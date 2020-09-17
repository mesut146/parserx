import dfa.DFA;
import dfa.NFA;
import grammar.GParser;
import grammar.ParseException;
import nodes.TokenDecl;
import nodes.Tree;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;

public class NfaTest {

    static Tree makeTree(String regex) throws ParseException {
        Tree tree = new Tree();
        GParser parser = new GParser(new StringReader(regex));
        tree.addToken(new TokenDecl("test", parser.regex()));
        return tree;
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

    static NFA makeNFA(File grammar) throws Exception {
        Tree tree = Tree.makeTree(grammar);
        NFA nfa = tree.makeNFA();
        System.out.println("total nfa states=" + nfa.numStates);
        //nfa.dump("");
        return nfa;
    }
}
