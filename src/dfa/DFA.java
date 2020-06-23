package dfa;

import nodes.Tree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DFA {
    public List<Transition>[] trans;
    public boolean[] accepting;
    public boolean[] isSkip;
    public String[] names;
    public int numStates;
    public int initial = 0;
    public static boolean debugTransition = false;
    public Tree tree;

    public DFA(int maxStates) {
        this.numStates = 0;
        //table = new int[maxStates][numInput];
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
        int[] arr = CharClass.desegment(input);
        if (debugTransition)
            System.out.printf("st:%d to st:%d with:(%s-%s) seg:%d\n", state, target, CharClass.printChar(arr[0]), CharClass.printChar(arr[1]), input);
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
                //group same targets
                Map<Integer, List<Integer>> map = new HashMap<>();//target -> input list
                for (Transition tr : list) {
                    List<Integer> l = map.get(tr.target);
                    if (l == null) {
                        l = new ArrayList<>();
                        map.put(tr.target, l);
                    }
                    l.add(tr.input);
                }
                list.clear();//remove all transitions
                for (Map.Entry<Integer, List<Integer>> e : map.entrySet()) {
                    //check the inputs neighbor
                    List<Integer> l = e.getValue();//inputs

                    int[] pre = CharClass.desegment(l.get(0));
                    if (l.size() == 1) {
                        list.add(new Transition(state, CharClass.segment(pre), e.getKey()));
                    }
                    for (int i = 1; i < l.size(); i++) {
                        int seg1 = l.get(i);
                        //int seg2 = e.getValue().get(i + 1);
                        int[] arr1 = CharClass.desegment(seg1);
                        //int[] arr2 = CharClass.desegment(seg2);
                        if (pre[1] + 1 == arr1[0]) {
                            pre[1] = arr1[1];//merge
                            //remove arr2
                        }
                        else {
                            //keep pre
                            list.add(new Transition(state, CharClass.segment(pre), e.getKey()));
                            pre = arr1;
                        }
                        if (i == l.size() - 1) {
                            list.add(new Transition(state, CharClass.segment(pre), e.getKey()));
                        }
                    }
                }
            }
        }
        System.out.println("optimized dfa");
    }

    public void dump(String path) {
        PrintWriter w = new PrintWriter(System.out);

        for (int state = initial; state <= numStates + 1; state++) {
            w.println(printState(state));

            List<Transition> arr = trans[state];
            if (arr != null) {
                for (Transition tr : arr) {
                    w.print("  ");
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
                if (isSkip[state]) {
                    w.printf("%d [color=blue]\n", state);
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
            System.out.println("dfa dot file writed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
