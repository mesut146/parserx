import dfa.DFA;
import dfa.NFA;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DfaTest {

    @Test
    public void javaLexer() throws Exception {
        DFA dfa = makeDFA(Env.getFile2("/javaLexer.g"));
        dfa.dump(null);
    }

    static DFA makeDFA(NFA nfa) throws FileNotFoundException {
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

    static void nfaToDfaTest() throws IOException {
        NFA nfa = new NFA(100);
        nfa.initial = 1;
        int zero = '0';
        int one = '1';
        nfa.addTransitionRange(1, 2, zero, zero);
        nfa.addEpsilon(1, 3);
        nfa.addEpsilon(3, 2);
        nfa.addTransitionRange(3, 4, zero, zero);
        nfa.addTransitionRange(4, 3, zero, zero);
        nfa.addTransitionRange(2, 2, one, one);
        nfa.addTransitionRange(2, 4, one, one);
        nfa.setAccepting(3, true);
        nfa.setAccepting(4, true);
        nfa.numStates = 4;
        nfa.dump("");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
    }

    //multiple
    static void nfaToDfaTest3() throws IOException {
        NFA nfa = new NFA(100);
        //nfa.initial = 0;
        nfa.addTransitionRange(0, 1, '0', '0');
        nfa.addTransitionRange(1, 2, 'x', 'x');
        nfa.addTransitionRange(0, 3, '0', '0');
        nfa.addTransitionRange(3, 4, 'x', 'x');
        nfa.addTransitionRange(3, 6, 't', 't');
        nfa.addTransitionRange(4, 5, 'y', 'y');
        nfa.addTransitionRange(0, 7, '1', '1');
        nfa.addTransitionRange(7, 1, '2', '2');
        nfa.addEpsilon(4, 1);
        nfa.addEpsilon(5, 1);
        nfa.setAccepting(2, true);
        nfa.setAccepting(5, true);
        nfa.setAccepting(6, true);
        nfa.numStates = 7;
        nfa.dump("");
        //nfa.dot(dir + "asd.dot");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
        //dfa.dot(dir + "/asd.dot");
    }

    //javapoint
    @Test
    public void nfaToDfaTest4() throws IOException {
        NFA nfa = new NFA(100);
        //nfa.initial = 0;
        nfa.addTransitionRange(0, 0, '0', '0');
        nfa.addEpsilon(0, 1);
        nfa.addTransitionRange(1, 1, '1', '1');
        nfa.addEpsilon(1, 2);
        nfa.addTransitionRange(2, 2, '2', '2');
        nfa.setAccepting(2, true);
        nfa.numStates = 2;
        //nfa.dump("");
        //nfa.dot("/home/mesut/IdeaProjects/parserx/test/asd.dot");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
        //dfa.dot(dir + "asd.dot");
    }

    static void nfaToDfaTest2() throws IOException {
        NFA nfa = new NFA(100);
        nfa.initial = 1;
        int zero = '0';
        int one = '1';
        nfa.addTransition(1, 2, 0);
        nfa.addEpsilon(1, 3);
        nfa.addEpsilon(3, 2);
        nfa.addTransition(3, 4, 0);
        nfa.addTransition(4, 3, 0);
        nfa.addTransition(2, 2, 1);
        nfa.addTransition(2, 4, 1);
        nfa.setAccepting(3, true);
        nfa.setAccepting(4, true);
        nfa.numStates = 4;
        nfa.dump("");
        //nfa.dot(testDir + "asd.dot");
        System.out.println("-------------");
        /*DFA dfa = nfa.dfa();
        dfa.dump("");*/
    }


}
