import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.utils.Helper;
import mesut.parserx.utils.NfaReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

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
    public void minimize() throws Exception {
        File file = Env.getResFile("fsm/dfa-min.dfa");
        NFA dfa = NfaReader.read(Helper.read(file));
        new Minimization().removeUnreachable(dfa);
        dfa = new Minimization().Hopcroft(dfa);
        dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void minimize2() throws Exception {
        NFA dfa = NFA.makeDFA(Env.getResFile("javaLexer.g"));
        new Minimization().removeUnreachable(dfa);
        dfa = new Minimization().Hopcroft(dfa);
        //dfa.dump(new PrintWriter(System.out));
        dfa = Minimization.combineAlphabet(dfa);
        dfa.dot(new FileWriter(Env.dotFile("dfa")));
    }
}
