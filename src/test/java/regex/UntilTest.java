package regex;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Test;

import java.io.FileWriter;

public class UntilTest {
    @Test
    public void test() throws Exception {
        Tree tree = Env.tree("until.g");
        NFA nfa = tree.makeNFA();
        NFA dfa = nfa.dfa();
        nfa.dot(new FileWriter(Env.dotFile("nfa.dot")));
        dfa.dot(new FileWriter(Env.dotFile("until.dot")));
        Node r = RegexBuilder.from(dfa);
        System.out.println(r);
    }
}
