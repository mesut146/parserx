package dfa;

import org.w3c.dom.Node;

//build regex from dfa
public class RegexBuilder {

    public static Node buildRegex(DFA dfa) {

        if (dfa.isAccepting(dfa.initial)) {
            int init = dfa.initial;
            dfa.initial = init - 1;
            dfa.setAccepting(init - 1, false);
        }

        return null;
    }
}
