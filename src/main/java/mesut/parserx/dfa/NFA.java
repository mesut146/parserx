package mesut.parserx.dfa;

import mesut.parserx.dfa.parser.NfaVisitor;
import mesut.parserx.nodes.Epsilon;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Range;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NFA {
    public static boolean debugTransition = false;
    public Tree tree;
    public int lastState = -1;
    public State initialState;
    public Map<Integer, State> id_to_state;
    public Map<String, State> modes = new HashMap<>();

    public NFA(int capacity, Tree tree) {
        this.tree = tree;
        this.id_to_state = new HashMap<>(capacity);
    }

    public NFA(int capacity) {
        this(capacity, new Tree());
    }

    public void init(int initial) {
        initialState = getState(initial);
        modes.put("DEFAULT", initialState);
    }

    public State newState() {
        return getState(++lastState);
    }

    public State getState(int id) {
        var res = id_to_state.get(id);
        if (res == null) {
            res = new State(id);
            id_to_state.put(id, res);
        }
        lastState = Math.max(lastState, id);
        return res;
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

    public boolean[] acc() {
        var res = new boolean[lastState + 1];
        for (var state : it()) {
            res[state.id] = state.accepting;
        }
        return res;
    }

    //epsilon transitions from a state
    StateSet getEps(State state) {
        var stateSet = new StateSet();
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

    public Alphabet getAlphabet() {
        return tree.alphabet;
    }

    public void addTransition(State state, State target, int input) {
        if (initialState == null) throw new RuntimeException("nfa must be initialized");
        if (debugTransition) {
            System.out.printf("%s -> %s, %s\n", state, target, getAlphabet().getRange(input));
        }
        Transition tr = new Transition(state, target, input);
        state.add(tr);
        lastState = Math.max(lastState, Math.max(state.id, target.id));
    }


    public void setAccepting(int state, boolean val) {
        getState(state).accepting = val;
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

    public boolean isAccepting(StateSet set) {
        return set.states.stream().anyMatch(s -> s.accepting);
    }

    public boolean isSkip(StateSet set) {
        return set.states.stream().anyMatch(s -> s.isSkip);
    }

    //get index of token by name
    public int indexOf(String name) {
        for (var tb : tree.tokenBlocks) {
            for (int i = 0; i < tb.tokens.size(); i++) {
                if (tb.tokens.get(i).name.equals(name)) {
                    return i;
                }
            }
            for (var mb : tb.modeBlocks) {
                for (int i = 0; i < mb.tokens.size(); i++) {
                    if (tb.tokens.get(i).name.equals(name)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    //get token name for state set by defined order
    String getName(StateSet set) {
        int minIndex = Integer.MAX_VALUE;
        String name = null;
        for (var state : set) {
            for (var nm : state.names) {
                int i = indexOf(nm);
                if (i < minIndex) {
                    name = nm;
                    minIndex = i;
                }
            }
        }
        return name;
    }

    public Iterable<State> it() {
        return () -> new Iterator<>() {
            int cur = initialState.id;

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

    public List<State> getTargets(State state, int input) {
        return state.transitions.stream()
                .filter(tr -> tr.input == input)
                .map(tr -> tr.target)
                .collect(Collectors.toList());
    }

    public void dump() {
        dump(new PrintWriter(System.out));
    }

    String getName(State state) {
        var names = state.names;
        if (names.isEmpty()) return "";
        if (names.size() == 1) return "(" + names.get(0) + ")";
        return "(" + names + ")";
    }

    public void dump(Writer writer) {
        PrintWriter w = new PrintWriter(writer);
        w.println("initial = " + initialState);
        for (var e : modes.entrySet()) {
            if (e.getKey().equals("DEFAULT")){
                continue;
            }
            w.printf("mode %s = %s;\n", e.getKey(), e.getValue());
        }
        w.print("final = ");
        boolean first = true;

        w.println(StreamSupport.stream(it().spliterator(), false)
                .filter(st -> st.accepting)
                .map(st -> st.id + getName(st))
                .collect(Collectors.joining(", ")));
//        for (var i : it()) {
//            if (i.accepting) {
//                if (!first) {
//                    w.print(", ");
//                }
//                w.print(i + getName(i));
//                first = false;
//            }
//        }
        w.println();
        for (var state : it()) {
            if (state.transitions.isEmpty()) continue;
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
        w.flush();
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
                w.printf("%d [shape = doublecircle color=%s xlabel=\"%s\"]\n", state.id, finalColor, name);
            }
            else if (state.isSkip) {
                w.printf("%d [color=%s xlabel=\"%s\"]\n", state.id, skipColor, name);
            }
            else {
                w.printf("%d [xlabel=\"%s\"]\n", state.id, name);
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

