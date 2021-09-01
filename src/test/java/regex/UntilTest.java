package regex;

import common.Env;
import lexer.Simulator;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Test;

import java.io.FileWriter;

public class UntilTest {
    @Test
    public void test() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("until.g"));
        NFA nfa = tree.makeNFA();
        NFA dfa = nfa.dfa();
        //new Simulator(dfa).simulate("/*asd*/");
        nfa.dot(new FileWriter("/media/mesut/SSD-DATA/IdeaProjects/parserx/dots/nfa.dot"));
        dfa.dot(new FileWriter("/media/mesut/SSD-DATA/IdeaProjects/parserx/dots/until.dot"));
        Node r = RegexBuilder.from(dfa);
        System.out.println(r);
    }
}
