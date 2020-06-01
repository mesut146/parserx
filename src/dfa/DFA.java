package dfa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
        System.out.printf("st:%d to st:%d with:(%s-%s) seg:%d\n", state, target, CharClass.printChar(arr[0]), CharClass.printChar(arr[1]), input);
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

    //merge segments
    public void optimize() {
        for (int state = initial; state <= numStates; state++) {
            List<Transition> list = trans[state];
            if (list != null) {
                Collections.sort(list, new Comparator<Transition>() {
                    @Override
                    public int compare(Transition o1, Transition o2) {
                        int[] seg1 = CharClass.desegment(o1.input);
                        int[] seg2 = CharClass.desegment(o2.input);
                        return seg1[0] - seg2[0];
                    }
                });
                Map<Integer, List<Integer>> map = new HashMap<>();//target -> input list
                for (Transition tr : list) {
                    List<Integer> l = map.get(tr.target);
                    if (l == null) {
                        l = new ArrayList<>();
                        map.put(tr.target, l);
                    }
                    l.add(tr.input);
                }
                for (Map.Entry<Integer, List<Integer>> e : map.entrySet()) {
                    //check the inputs neighbor
                    for (int i = 0; i < e.getValue().size(); i++) {
                        int seg1 = e.getValue().get(i);
                        int seg2 = e.getValue().get(i + 1);
                        int[] arr1 = CharClass.desegment(seg1);
                        int[] arr2 = CharClass.desegment(seg2);
                        if (arr1[1] + 1 == arr2[0]) {
                            arr1[1] = arr2[1];
                            //remove arr2
                        }
                    }
                }
            }
        }
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
                    w.print(CharClass.seg2str(tr.input));

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

    public void dot(String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            PrintWriter w = new PrintWriter(bw);
            w.println("digraph G{");
            w.println("rankdir = LR");
            w.printf("%d [color=red]\n", initial);
            for (int state = initial; state <= numStates; state++) {
                if (isAccepting(state)) {
                    w.printf("%d [shape = doublecircle]\n", state);
                }
            }

            for (int state = initial; state <= numStates; state++) {
                List<Transition> list = trans[state];
                if (list != null) {
                    for (Transition tr : list) {
                        w.printf("%s -> %s [label=\"[%s]\"]\n", state, tr.target, CharClass.seg2escaped(tr.input));
                    }
                }
            }
            w.println("}");
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
