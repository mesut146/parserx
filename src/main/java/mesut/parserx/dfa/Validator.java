package mesut.parserx.dfa;

import java.util.HashSet;
import java.util.Set;

public class Validator {
    //true if dfa false if nfa
    public static boolean isDFA(NFA dfa) {
        for (int state : dfa.it()) {
            Set<Integer> inputs = new HashSet<>();
            for (Transition tr : dfa.get(state)) {
                if (!inputs.add(tr.input)) {
                    return false;
                }
            }
        }
        return true;
    }

}
