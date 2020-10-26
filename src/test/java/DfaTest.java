import dfa.DFA;
import dfa.NFA;
import org.junit.Test;

import java.io.File;

public class DfaTest {

    @Test
    public void javaLexer() throws Exception {
        DFA dfa = makeDFA(Env.getFile2("/javaLexer.g"));
        dfa.dump(null);
    }

    static DFA makeDFA(NFA nfa) {
        System.out.println("-----DFA-----");
        DFA dfa = nfa.dfa();
        System.out.println("total dfa states=" + dfa.numStates);
        //dfa.dump("");
        return dfa;
    }

    public static DFA makeDFA(File grammar) throws Exception {
        NFA nfa = NfaTest.makeNFA(grammar);
        nfa.dot(grammar.getAbsolutePath() + "-nfa.dot");
        DFA dfa = makeDFA(nfa);
        dfa.dot(grammar.getAbsolutePath() + "-dfa.dot");
        return dfa;
    }


}
