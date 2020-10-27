package dfa;

import nodes.Tree;
import utils.UnicodeUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DFA {
    public static boolean debugTransition = false;
    public List<Transition>[] trans;
    public boolean[] accepting;
    public boolean[] isSkip;
    public String[] names;
    public int numStates;
    public int initial = 0;
    public Tree tree;

    public Alphabet getAlphabet() {
        return tree.alphabet;
    }

    public DFA(int maxStates) {
        this.numStates = 0;
        trans = new List[maxStates];
        accepting = new boolean[maxStates];
        isSkip = new boolean[maxStates];
        this.numStates = 0;
        names = new String[maxStates];
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        int len = trans.length;
        List<Transition>[] newTrans = new List[max];
        boolean[] newAccepting = new boolean[max];
        boolean[] newSkip = new boolean[max];
        String[] newNames = new String[max];
        System.arraycopy(trans, 0, newTrans, 0, len);
        System.arraycopy(accepting, 0, newAccepting, 0, len);
        System.arraycopy(isSkip, 0, newSkip, 0, len);
        System.arraycopy(names, 0, newNames, 0, len);
        trans = newTrans;
        accepting = newAccepting;
        isSkip = newSkip;
        names = newNames;
    }

    public void addTransition(int state, int input, int target) {
        expand(state);
        if (debugTransition) {
            System.out.printf("st:%d to st:%d with:%s\n", state, target, tree.alphabet.getRange(input));
        }
        List<Transition> tr = trans[state];
        if (tr == null) {
            tr = new ArrayList<>();
            trans[state] = tr;
        }
        tr.add(new Transition(state, input, target));
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
        return "states=" + numStates;
    }

    //merge transitions
    public void merge() {
        /*
        for (int state = initial; state <= numStates; state++) {
            List<Transition> list = trans[state];
            if (list != null) {
                //target -> input list
                Map<Integer, List<Integer>> map = new HashMap<>();
                for (Transition tr : list) {
                    if (map.containsKey(tr.target)) {
                        map.get(tr.target).add(tr.input);
                    }
                    else {
                        List<Integer> arr = new ArrayList<>();
                        arr.add(tr.input);
                        map.put(tr.target, arr);
                    }
                }
                for (int target : map.keySet()) {
                    List<Integer> inputs = map.get(target);

                }
            }
        }*/
        System.out.println("dfa merged");
    }

    public void dump(File path) {
        OutputStream os = System.out;
        if (path != null) {
            try {
                os = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        PrintWriter w = new PrintWriter(os);

        for (int state = initial; state <= numStates + 1; state++) {
            w.println(printState(state));

            List<Transition> arr = trans[state];
            if (arr != null) {
                Collections.sort(arr, new Comparator<Transition>() {
                    @Override
                    public int compare(Transition o1, Transition o2) {
                        return getAlphabet().getRange(o1.input).compareTo(getAlphabet().getRange(o2.input));
                    }
                });
                for (Transition tr : arr) {
                    w.print("  ");
                    w.print(tree.alphabet.getRange(tr.input));
                    w.print(" -> ");
                    w.print(printState(tr.target));
                    w.println();
                }
            }
            w.println();
        }
        w.flush();
        w.close();
    }

    String printState(int st) {
        if (isAccepting(st)) {
            return "(S" + st + ", " + names[st] + ")";
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
                if (isSkip[state]) {
                    w.printf("%d [color=blue]\n", state);
                }
            }

            for (int state = initial; state <= numStates; state++) {
                List<Transition> list = trans[state];
                if (list != null) {
                    for (Transition tr : list) {
                        w.printf("%s -> %s [label=\"[%s]\"]\n", state, tr.target, UnicodeUtils.escapeString(tree.alphabet.getRange(tr.input).toString()));
                    }
                }
            }
            w.println("}");
            w.flush();
            w.close();
            System.out.println("dfa dot file writed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
