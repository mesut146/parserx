package dfa;

//each dfa is a nfa
public class DFA extends NFA {

    public DFA(int maxStates) {
        super(maxStates);
    }

    @Override
    public void addEpsilon(int state, int target) {
        throw new RuntimeException("epsilon in dfa");
    }
}
