package mesut.parserx.dfa;

import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Range;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

@SuppressWarnings("unchecked")
public class NFA {
    public static boolean debugTransition = false;
    public Tree tree;
    public List<Transition>[] trans;
    public boolean[] accepting;
    public boolean[] isSkip;
    public List<String>[] names;
    public int initial = 0;
    public int lastState = 0;

    public NFA(int maxStates, Tree tree) {
        trans = new List[maxStates];
        accepting = new boolean[maxStates];
        isSkip = new boolean[maxStates];
        names = new List[maxStates];
        this.tree = tree;
    }

    public NFA(int maxStates) {
        this(maxStates, new Tree());
    }

    public static NFA makeDFA(File path) {
        return Tree.makeTree(path).makeNFA().dfa();
    }

    public static NFA makeNFA(File path) {
        return Tree.makeTree(path).makeNFA();
    }

    public NFA dfa() {
        return new DFABuilder(this).dfa();
    }

    public void expand(int state) {
        if (state < trans.length) {
            return;
        }
        int max = state * 2;
        int len = trans.length;

        accepting = expand(accepting, new boolean[max], len);
        trans = expand(trans, new List[max], len);
        isSkip = expand(isSkip, new boolean[max], len);
        names = expand(names, new List[max], len);
    }

    <T> T expand(Object src, Object target, int len) {
        System.arraycopy(src, 0, target, 0, len);
        return (T) target;
    }

    //epsilon transitions from a state
    StateSet getEps(int state) {
        StateSet stateSet = new StateSet();
        for (Transition tr : get(state)) {
            if (tr.epsilon) {
                stateSet.addState(tr.target);
            }
        }
        return stateSet;
    }

    public boolean isDead(int s) {
        return !hasTransitions(s) && findIncoming(s).isEmpty();
    }

    public boolean hasTransitions(int state) {
        return trans[state] != null && !trans[state].isEmpty();
    }

    public Alphabet getAlphabet() {
        return tree.alphabet;
    }

    //state,input index,target state
    public void addTransition(int state, int target, int input) {
        if (debugTransition) {
            System.out.printf("st:%d to st:%d with:%s\n", state, target, getAlphabet().getRange(input));
        }
        Transition transition = new Transition(state, target, input);
        add(transition);
        lastState = Math.max(lastState, Math.max(state, target));
    }

    private void add(Transition tr) {
        expand(Math.max(tr.state, tr.target));
        List<Transition> arr = trans[tr.state];
        if (arr == null) {
            arr = new ArrayList<>();
            trans[tr.state] = arr;
        }
        if (!arr.contains(tr)) {
            arr.add(tr);
        }
    }

    public void addTransitionRange(int state, int target, int left, int right) {
        if (debugTransition)
            System.out.printf("st:%d (%s-%s) to st:%d\n", state, UnicodeUtils.printChar(left), UnicodeUtils.printChar(right), target);
        addTransition(state, target, getAlphabet().getId(Range.of(left, right)));
    }

    public void setAccepting(int state, boolean val) {
        expand(state);
        accepting[state] = val;
    }

    public boolean isAccepting(int state) {
        return accepting[state];
    }

    public void addEpsilon(int state, int target) {
        if (!getEps(state).contains(target)) {
            add(new Transition(state, target));
        }
    }

    public List<Transition> findIncoming(int to) {
        List<Transition> all = new ArrayList<>();
        for (int state : it()) {
            for (Transition transition : get(state)) {
                if (transition.target == to) {
                    all.add(transition);
                }
            }
        }
        return all;
    }

    public int newState() {
        return ++lastState;
    }

    public boolean isAccepting(StateSet set) {
        for (int state : set) {
            if (isAccepting(state)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSkip(StateSet set) {
        for (int state : set) {
            if (isSkip[state]) {
                return true;
            }
        }
        return false;
    }

    public void addName(String name, int state) {
        List<String> list = names[state];
        if (list == null) {
            list = new ArrayList<>();
            names[state] = list;
        }
        if (!list.contains(name)) {
            list.add(name);
        }
    }

    //get token name for state set as defined order
    String getName(StateSet set) {
        int minIndex = Integer.MAX_VALUE;
        String name = null;
        for (int state : set) {
            if (names[state] != null) {
                List<String> list = names[state];
                for (String nm : list) {
                    int i = tree.indexOf(nm);
                    if (i < minIndex) {
                        name = nm;
                        minIndex = i;
                    }
                }
            }
        }
        return name;
    }

    public List<Transition> get(int state) {
        if (hasTransitions(state)) {
            return trans[state];
        }
        return new ArrayList<>();
    }

    public Iterable<Integer> it() {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int cur = 0;

                    @Override
                    public boolean hasNext() {
                        return cur <= lastState;
                    }

                    @Override
                    public Integer next() {
                        return cur++;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
    }

    //get target state using state and input
    public int getTarget(int state, int input) {
        for (Transition tr : get(state)) {
            if (tr.input == input) return tr.target;
        }
        return -1;
    }

    public void dump() {
        dump(new PrintWriter(System.out));
    }

    public void dump(Writer writer) {
        PrintWriter w = new PrintWriter(writer);
        w.println("initial=" + initial);
        w.print("final=");
        for (int i : it()) {
            if (isAccepting(i)) {
                w.print(i + (names[i] != null ? "(" + names[i] + ") " : " "));
            }
        }
        w.println();
        for (int state = 0; state <= lastState; state++) {
            if (!hasTransitions(state)) continue;
            List<Transition> arr = trans[state];
            sort(arr);
            for (Transition tr : arr) {
                w.print(state + " -> " + tr.target);
                if (!tr.epsilon) {
                    w.print("  , ");
                    w.print(getAlphabet().getRegex(tr.input));
                }
                w.println();
            }
        }
        w.close();
    }

    private void sort(List<Transition> arr) {
        Collections.sort(arr, new Comparator<Transition>() {
            @Override
            public int compare(Transition o1, Transition o2) {
                Node r1 = getAlphabet().getRegex(o1.input);
                Node r2 = getAlphabet().getRegex(o2.input);
                if (r1.isRange() && r2.isRange()) {
                    return r1.asRange().compareTo(r2.asRange());
                }
                return 0;
            }
        });
    }

    public void dot(Writer writer) {
        String finalColor = "red";
        String initialColor = "red";
        String skipColor = "blue";
        PrintWriter w = new PrintWriter(writer);
        w.println("digraph G{");
        w.println("rankdir = LR");
        w.printf("%d [color=%s]\n", initial, initialColor);
        for (int state : it()) {
            if (isDead(state)) continue;
            if (isAccepting(state)) {
                w.printf("%d [shape = doublecircle color=%s xlabel=\"%s\"]\n", state, finalColor, names[state] == null ? "" : names[state]);
            }
            if (isSkip[state]) {
                w.printf("%d [color=%s xlabel=\"%s\"]\n", state, skipColor, names[state] == null ? "" : names[state]);
            }
        }

        for (int state : it()) {
            for (Transition tr : get(state)) {
                String label;
                if (tr.epsilon) {
                    label = "ε";
                }
                else {
                    label = UnicodeUtils.escapeString(getAlphabet().getRegex(tr.input).toString());
                }
                w.printf("%s -> %s [label=\"%s\"]\n", state, tr.target, label);
            }
        }
        w.println("}");
        w.close();
    }
}

