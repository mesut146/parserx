package dfa;

import org.w3c.dom.Node;

//build regex from dfa
public class RegexBuilder {

    public static void test() {
        DFA dfa = new DFA(100);
        dfa.addTransition(0, 'x', 1);
        dfa.addTransition(1, 'a', 2);
        dfa.addTransition(2, 'b', 3);
        dfa.addTransition(3, 'c', 4);
        dfa.addTransition(4, 'a', 1);
    }


    public static Node buildRegex(DFA dfa) {

        if (dfa.isAccepting(dfa.initial)) {
            int init = dfa.initial;
            dfa.initial = init - 1;
            dfa.setAccepting(init - 1, false);
        }

        return null;
    }
}
