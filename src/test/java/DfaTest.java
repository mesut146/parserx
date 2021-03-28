import mesut.parserx.dfa.DFA;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.utils.Helper;
import mesut.parserx.utils.NfaReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

public class DfaTest {

    static DFA makeDFA(NFA nfa) {
        System.out.println("-----DFA-----");
        DFA dfa = nfa.dfa();
        System.out.println("total dfa states=" + dfa.lastState);
        //dfa.dump("");
        return dfa;
    }

    public static DFA makeDFA(File grammar) throws IOException {
        NFA nfa = NfaTest.makeNFA(grammar);
        DFA dfa = makeDFA(nfa);

        nfa.dot(new FileWriter(grammar.getAbsolutePath() + "-nfa.dot"));
        dfa.dot(new FileWriter(grammar.getAbsolutePath() + "-dfa.dot"));
        return dfa;
    }

    @Test
    @Ignore
    public void javaLexer() throws Exception {
        DFA dfa = makeDFA(Env.getFile2("/javaLexer.g"));
        dfa.dump(null);
    }

    @Test
    public void minimize() throws Exception {
        DFA dfa = (DFA) NfaReader.read(Helper.read(new FileInputStream(Env.getResFile("fsm/dfa-min.dfa"))));
        new Minimization().removeUnreachable(dfa);
        //dfa.dump(new PrintWriter(System.out));
        new Minimization().Hopcroft(dfa);
    }
}
