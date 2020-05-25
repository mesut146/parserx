package dfa;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DFA {
    //[curState][inputChar]=nextState
    //public int[][] table;
    List<Transition>[] trans;
    public boolean[] accepting;
    int numStates;
    int numInput;
    public int initial = 0;

    public DFA() {
        this(500, 255);
    }

    public DFA(int maxStates, int numInput) {
        this.numInput = numInput;
        this.numStates = 0;
        //table = new int[maxStates][numInput];
        trans = new List[maxStates];
        accepting = new boolean[maxStates];
        this.numStates = 0;
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        int len = trans.length;
        //int[][] newTable = new int[max][numInput];
        List<Transition>[] newTrans = new List[max];
        boolean[] newAccepting = new boolean[max];
        //System.arraycopy(table, 0, newTable, 0, numStates);
        System.arraycopy(trans, 0, newTrans, 0, len);
        System.arraycopy(accepting, 0, newAccepting, 0, len);
        //table = newTable;
        trans = newTrans;
        accepting = newAccepting;
    }

    public void addTransition(int state, int input, int target) {
        int[] arr = CharClass.desegment(input);
        System.out.printf("st:%d (%s-%s) to st:%d seg:%d\n", state, CharClass.printChar(arr[0]), CharClass.printChar(arr[1]), target, input);
        List<Transition> tr = trans[state];
        if (tr == null) {
            tr = new ArrayList<>();
            trans[state] = tr;
        }
        tr.add(new Transition(state, input, target));
        //table[state][input] = target;
    }

    /*public int getTransition(int state, int input) {
        return table[state][input];
    }*/
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

    public void dump(String path) {
        PrintWriter w = new PrintWriter(System.out);

        for (int state = initial; state <= numStates + 1; state++) {
            w.println(printState(state));

            List<Transition> arr = trans[state];
            if (arr != null) {
                for (Transition tr : arr) {
                    w.print("  ");
                    //int seg = getSegment(tr.symbol);
                    w.print(CharClass.seg2str(tr.symbol));

                    w.print(" -> ");
                    w.print(printState(tr.target));
                    w.println();
                }
            }
            w.println();
        }
        w.flush();
    }

    String printState(int st) {
        if (isAccepting(st)) {
            return "(S" + st + ")";
        }
        return "S" + st;
    }
}
