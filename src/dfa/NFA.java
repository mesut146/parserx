package dfa;

import dfa.DFA;

public class NFA {
    //[curState][input]=nextStateSet
    StateSet[][] table;
    boolean[] accepting;
    //[curState][nextState]=isEpsilon
    boolean[][] epsilon;
    int stateCount = 1;
    int numStates;
    int numInput;

    public NFA(int numStates) {
        table = new StateSet[numStates][255];
        accepting = new boolean[numStates];
        epsilon = new boolean[numStates][numStates];
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        StateSet[][] newTable = new StateSet[max][numInput];
        boolean[] newAccepting = new boolean[max];
        System.arraycopy(table, 0, newTable, 0, numStates);
        System.arraycopy(accepting, 0, newAccepting, 0, numStates);
        table = newTable;
        accepting = newAccepting;
    }

    public void addTransition(int state, int input, int target) {
        StateSet set = table[state][input];
        if (set == null) {
            set = new StateSet();
            table[state][input] = set;
        }
        set.addState(target);
    }

    public void addTransition(int state, int input, StateSet targets) {
        StateSet set = table[state][input];
        if (set == null) {
            set = new StateSet();
            table[state][input] = set;
        }
        for (int target : targets.states) {
            set.addState(target);
        }
    }

    public void addTransition(StateSet states, int input, int target) {
        for (int state : states.states) {
            addTransition(state, input, target);
        }
    }

    public StateSet getTransition(int state, int input) {
        return table[state][input];
    }

    public void setAccepting(int state, boolean val) {
        accepting[state] = val;
    }

    public void setAccepting(StateSet states, boolean val) {
        for (int state : states.states) {
            accepting[state] = val;
        }
    }

    public boolean isAccepting(int state) {
        return accepting[state];
    }

    public void addEpsilon(int state, int target) {
        epsilon[state][target] = true;
    }

    public DFA dfa() {
        DFA dfa = new DFA(table.length * 2, 255);

        return dfa;
    }
}

