package mesut.parserx.dfa;

import mesut.parserx.dfa.parser.NfaVisitor;
import mesut.parserx.nodes.Epsilon;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Range;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.*;
import java.util.*;

public class NFA {
    public static boolean debugTransition = false;
    public Tree tree;
    public int lastState = -1;
    public State initialState;
    State[] id_to_state;
    private int capacity;

    public NFA(int capacity, Tree tree) {
        this.capacity = capacity;
        this.tree = tree;
        id_to_state = new State[capacity];
        this.initialState = getState(0);
    }

    public NFA(int maxStates) {
        this(maxStates, new Tree());
    }

    public static NFA read(File file) throws IOException {
        return NfaVisitor.make(file);
    }

    public static NFA read(String str) throws IOException {
        return NfaVisitor.make(str);
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
        if (state < capacity) {
            return;
        }
        int new_cap = state * 2;
        var new_arr = new State[new_cap];
        System.arraycopy(id_to_state, 0, new_arr, 0, capacity);

        id_to_state = new_arr;
        capacity = new_cap;
    }


    public boolean[] acc() {
        var res = new boolean[lastState+1];
        for (int i = 0; i <= lastState; i++) {
            res[i] = getState(i).accepting;
        }
        return res;
    }

    //epsilon transitions from a state
    StateSet getEps(State state) {
        StateSet stateSet = new StateSet();
        for (Transition tr : state.transitions) {
            if (tr.epsilon) {
                stateSet.addState(tr.target);
            }
        }
        return stateSet;
    }

    public boolean isDead(State state) {
        return state.transitions.isEmpty() && findIncoming(state).isEmpty();
    }

    public boolean hasTransitions(int state) {
        return !get(state).isEmpty();
    }

    public Alphabet getAlphabet() {
        return tree.alphabet;
    }

    //state,input index,target state
    public void addTransition(int state, int target, int input) {
        if (debugTransition) {
            System.out.printf("%s -> %s, %s\n", state, target, getAlphabet().getRange(input));
        }
        Transition tr = new Transition(getState(state), getState(target), input);
        getState(state).add(tr);
        lastState = Math.max(lastState, Math.max(state, target));
    }

    public State getState(int id) {
        expand(id);
        var res = id_to_state[id];
        if (res == null) {
            res = new State(id);
            id_to_state[id] = res;
        }
        return res;
    }

    public void addTransitionRange(int state, int target, int left, int right) {
        if (debugTransition)
            System.out.printf("st:%d (%s-%s) to st:%d\n", state, UnicodeUtils.printChar(left), UnicodeUtils.printChar(right), target);
        addTransition(state, target, getAlphabet().getId(Range.of(left, right)));
    }

    public void setAccepting(int state, boolean val) {
        getState(state).accepting = val;
    }


    public void addEpsilon(int state, int target) {
        getState(state).add(new Transition(getState(state), getState(target)));
    }

    public List<Transition> findIncoming(State to) {
        List<Transition> all = new ArrayList<>();
        for (var state : it()) {
            for (Transition transition : state.transitions) {
                if (transition.target.equals(to)) {
                    all.add(transition);
                }
            }
        }
        return all;
    }

    public State newState() {
        return getState(++lastState);
    }

    public boolean isAccepting(StateSet set) {
        for (var state : set) {
            if (state.accepting) {
                return true;
            }
        }
        return false;
    }

    public boolean isSkip(StateSet set) {
        for (var state : set) {
            if (state.isSkip) {
                return true;
            }
        }
        return false;
    }

    public void addName(String name, int state) {
        List<String> list = getState(state).names;
        if (!list.contains(name)) {
            list.add(name);
        }
    }

    //get token name for state set by defined order
    String getName(StateSet set) {
        int minIndex = Integer.MAX_VALUE;
        String name = null;
        for (var state : set) {
            for (String nm : state.names) {
                int i = tree.indexOf(nm);
                if (i < minIndex) {
                    name = nm;
                    minIndex = i;
                }
            }
        }
        return name;
    }

    public List<Transition> get(int state) {
        return getState(state).transitions;
    }

    public Iterable<State> it() {
        return () -> new Iterator<>() {
            int cur = 0;

            @Override
            public boolean hasNext() {
                return cur <= lastState;
            }

            @Override
            public State next() {
                return getState(cur++);
            }

            @Override
            public void remove() {
            }
        };
    }

    //get target state using state and input
    public State getTarget(State state, int input) {
        for (Transition tr : state.transitions) {
            if (tr.input == input) return tr.target;
        }
        return null;
    }

    public void dump() {
        dump(new PrintWriter(System.out));
    }

    String getName(int i) {
        var names = getState(i).names;
        if (names.isEmpty()) return "";
        if (names.size() == 1) return "(" + names.get(0) + ")";
        return "(" + names + ")";
    }

    public void dump(Writer writer) {
        PrintWriter w = new PrintWriter(writer);
        w.println("initial = " + initialState);
        w.print("final = ");
        boolean first = true;
        for (var i : it()) {
            if (i.accepting) {
                if (!first) {
                    w.print(", ");
                }
                w.print(i + getName(i.state));
                first = false;
            }
        }
        w.println();
        for (var state : it()) {
            if (!state.transitions.isEmpty()) continue;
            List<Transition> arr = state.transitions;
            sort(arr);
            for (Transition tr : arr) {
                w.print(state + " -> " + tr.target);
                if (!tr.epsilon) {
                    w.print("  , ");
                    Node input = getAlphabet().getRegex(tr.input);
                    if (input.isString()) {
                        w.print(input.asString().printNormal());
                    }
                    else if (input.isRange()) {
                        Range range = input.asRange();
                        w.print(range);
                    }
                    else {
                        w.print(input);
                    }
                }
                w.println();
            }
        }
        w.close();
    }

    private void sort(List<Transition> arr) {
        arr.sort((o1, o2) -> {
            Node r1 = getAlphabet().getRegex(o1.input);
            Node r2 = getAlphabet().getRegex(o2.input);
            if (r1.isRange() && r2.isRange()) {
                return r1.asRange().compareTo(r2.asRange());
            }
            return 0;
        });
    }

    public void dot(File path) throws IOException {
        dot(new FileWriter(path));
    }

    public void dot(Writer writer) {
        String finalColor = "red";
        String initialColor = "red";
        String skipColor = "blue";
        PrintWriter w = new PrintWriter(writer);
        w.println("digraph G{");
        w.println("rankdir = LR;");
        w.printf("%s [color=%s]\n", initialState, initialColor);
        for (var state : it()) {
            if (isDead(state)) continue;
            var names = state.names;
            String name = names.isEmpty() ? "" : names.toString();
            if (names.size() == 1) {
                name = names.get(0);
            }
            if (state.accepting) {
                //todo write label inside
                w.printf("%d [shape = doublecircle color=%s xlabel=\"%s\"]\n", state.state, finalColor, name);
            }
            else if (state.isSkip) {
                w.printf("%d [color=%s xlabel=\"%s\"]\n", state.state, skipColor, name);
            }
            else {
                w.printf("%d [xlabel=\"%s\"]\n", state.state, name);
            }
        }

        for (var state : it()) {
            for (Transition tr : state.transitions) {
                String label;
                if (tr.epsilon) {
                    label = Epsilon.str();
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

