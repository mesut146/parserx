package dfa;

public class DFA {
    //[curState][inputChar]=nextState
    public int[][] table;
    public boolean[] accepting;
    int numStates;
    int numInput;
    int initial = 0;

    public DFA() {
        this(500, 255);
    }

    public DFA(int maxStates, int numInput) {
        this.numInput = numInput;
        this.numStates = 0;
        table = new int[maxStates][numInput];
        accepting = new boolean[maxStates];
        this.numStates = 0;
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        int[][] newTable = new int[max][numInput];
        boolean[] newAccepting = new boolean[max];
        System.arraycopy(table, 0, newTable, 0, numStates);
        System.arraycopy(accepting, 0, newAccepting, 0, numStates);
        table = newTable;
        accepting = newAccepting;
    }

    public void addTransition(int state, int input, int target) {
        table[state][input] = target;
    }

    public int getTransition(int state, int input) {
        return table[state][input];
    }

    public void setAccepting(int state, boolean val) {
        accepting[state] = val;
    }

    public boolean isAccepting(int state) {
        return accepting[state];
    }

    public int newState() {
        return ++numStates;
    }

    @Override
    public String toString() {
        return "states=" + numStates + " inputs=" + numInput;
    }
}
