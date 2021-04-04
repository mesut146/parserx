import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Helper;
import mesut.parserx.utils.NfaReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DfaTest {

    static NFA makeDFA(NFA nfa) {
        System.out.println("-----DFA-----");
        NFA dfa = nfa.dfa();
        System.out.println("total dfa states=" + dfa.lastState);
        //dfa.dump("");
        return dfa;
    }

    public static NFA makeDFA(File grammar) throws IOException {
        NFA nfa = NfaTest.makeNFA(grammar);
        NFA dfa = makeDFA(nfa);

        nfa.dot(new FileWriter(grammar.getAbsolutePath() + "-nfa.dot"));
        dfa.dot(new FileWriter(grammar.getAbsolutePath() + "-dfa.dot"));
        return dfa;
    }

    @Test
    @Ignore
    public void javaLexer() throws Exception {
        NFA dfa = makeDFA(Env.getFile2("/javaLexer.g"));
        dfa.dump(null);
    }

    @Test
    public void hopcroft() throws Exception {
        //File file = Env.getResFile("fsm/dfa-min.dfa");
        File file = Env.getResFile("fsm/dfa2.dfa");
        NFA dfa = NfaReader.read(Helper.read(file));
        Minimization.removeUnreachable(dfa);
        dfa = new Minimization().Hopcroft(dfa);
        dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void minimizeMy() throws Exception {
        File file = Env.getResFile("fsm/dfa-min.dfa");
        //File file = Env.getResFile("fsm/dfa2.dfa");
        NFA dfa = NfaReader.read(Helper.read(file));
        Minimization.removeUnreachable(dfa);
        Minimization.removeDead(dfa);
        new Minimization().optimize(dfa);
        //dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void minimize2() throws Exception {
        NFA dfa = NFA.makeDFA(Env.getResFile("javaLexer.g"));
        System.out.println("before " + Minimization.numOfStates(dfa));
        Minimization.removeUnreachable(dfa);
        Minimization.removeDead(dfa);
        //dfa = new Minimization().Hopcroft(dfa);
        dfa = new Minimization().optimize(dfa);
        dfa = Minimization.combineAlphabet(dfa);
        System.out.println("after " + Minimization.numOfStates(dfa));
        dfa.dot(new FileWriter(Env.dotFile("dfa")));
    }

    @Test
    public void name() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("min/a.g"));
        NFA dfa = tree.makeNFA().dfa();
        dfa.dot(new FileWriter(Env.dotFile("dfa")));
        dfa.dump(new PrintWriter(System.out));
    }
}
