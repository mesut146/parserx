package mesut.parserx.dfa;

import java.util.HashSet;
import java.util.Set;

public class Validator {

    //true if dfa false if nfa
    public static boolean isDFA(NFA dfa) {
        for (var state : dfa.it()) {
            Set<Integer> inputs = new HashSet<>();
            for (Transition tr : state.transitions) {
                //epsilon or more than one input transition
                if (tr.epsilon || !inputs.add(tr.input)) {
                    return false;
                }
            }
        }
        return true;
    }

}
