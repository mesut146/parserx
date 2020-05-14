package dfa;

import nodes.*;
import rule.NameNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class NFA {
    public Tree tree;
    //StateSet[][] table;//[curState][input]=nextStateSet
    boolean[] accepting;//[state]=isAccepting
    StateSet[] epsilon;//[state]=set of next states with epsilon moves
    public int numStates;
    public int numInput;//alphabet
    int initial = 0;
    public List<Transition> trans;//state,input,targets
    public HashMap<Integer, Integer> alphabet;//code point(segment) to index
    public HashMap<Integer, Set<Integer>> inputMap;//state to input set
    public HashMap<Integer, Set<Integer>> transMap;//state to target state set

    public NFA(int numStates) {
        //table = new StateSet[numStates][255];
        trans = new ArrayList<>();
        accepting = new boolean[numStates];
        epsilon = new StateSet[numStates];
        this.numStates = 0;
        this.numInput = 0;
        alphabet = new HashMap<>();
        inputMap = new HashMap<>();
        transMap = new HashMap<>();
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        StateSet[][] newTable = new StateSet[max][numInput];
        boolean[] newAccepting = new boolean[max];
        StateSet[] newEpsilon = new StateSet[max];
        //System.arraycopy(table, 0, newTable, 0, numStates);
        System.arraycopy(accepting, 0, newAccepting, 0, numStates);
        System.arraycopy(epsilon, 0, newEpsilon, 0, numStates);
        //table = newTable;
        accepting = newAccepting;
        epsilon = newEpsilon;
    }

    //convert code point(segment) to index
    int checkInput(int input) {
        if (input == -1) {
            //System.out.println("aaa");
        }
        Integer index = alphabet.get(input);
        if (index == null) {
            numInput++;
            index = numInput;
            alphabet.put(input, index);
        }
        return index;
    }

    void addInputMap(int state, int input) {
        Set<Integer> s = inputMap.get(state);
        if (s == null) {
            s = new HashSet<>();
            inputMap.put(state, s);
        }
        s.add(input);
    }

    void addTransMap(int state, int target) {
        Set<Integer> s = transMap.get(state);
        if (s == null) {
            s = new HashSet<>();
            transMap.put(state, s);
        }
        s.add(target);
    }

    //state index,input index,target state index
    public void addTransition(int state, int input, int target) {
        addInputMap(state, input);
        addTransMap(state, target);
        Transition tr;
        if (trans.size() < state) {
            tr = new Transition();
        }
        else {
            tr = trans.get(state);
            if (tr.state != state) {
                tr = new Transition();
            }
        }
        tr.state = state;
        tr.symbol = input;
        tr.states.add(target);
        trans.set(state, tr);
    }

    public void addTransitionRange(int state, int target, int left, int right) {
        int seg = segment(left, right);
        int idx = checkInput(seg);
        addTransition(state, idx, target);
    }

    /*public void addTransition(int state, int input, StateSet targets) {
        StateSet set = table[state][input];
        if (set == null) {
            set = new StateSet();
            table[state][input] = set;
        }
        for (int target : targets.states) {
            set.addState(target);
        }
    }*/

    /*public void addTransition(StateSet states, int input, int target) {
        for (int state : states.states) {
            addTransition(state, input, target);
        }
    }*/

    /*public StateSet getTransition(int state, int input) {
        return table[state][input];
    }*/

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
        //epsilon[state][target] = true;
        StateSet set = epsilon[state];
        if (set == null) {
            set = epsilon[state] = new StateSet();
        }
        set.addState(target);
    }

    //todo
    public DFA dfa() {
        DFA dfa = new DFA(table.length * 2, numInput);
        Map<StateSet, Integer> map = new HashMap<>();
        for (int state = initial; state < numStates; state++) {
            StateSet set = closure(state);
            System.out.println(set.states);
            for (int input : inputMap.get(state)) {
                StateSet in = table[state][input];
                if (in != null) {
                    int ns;
                    if (map.containsKey(in)) {
                        ns = map.get(in);
                    }
                    else {
                        ns = dfa.newState();
                        map.put(in, ns);
                    }
                    dfa.addTransition(state, input, ns);
                }
            }
        }
        return dfa;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(int state) {
        StateSet res = new StateSet();
        res.addState(state);
        StateSet s = epsilon[state];
        if (s == null || s.states.size() == 0) {
            return res;
        }
        //res.addAll(s);
        for (int i : s.states) {
            res.addAll(closure(i));
        }
        return res;
    }

    //add regex to @start
    //return start,end state
    public Pair insert(Node node, int start) {
        Pair p = new Pair(start + 1, start + 1);
        if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st = start;
            for (Node child : seq.list) {
                st = insert(child, st).end;
            }
            p.end = st;
        }
        else if (node.is(Bracket.class)) {
            Bracket b = node.as(Bracket.class);
            if (b.negate) {
                //todo all at once
                int end = newState();//order not matter?
                for (int i = 0; i < b.list.size(); i++) {
                    Node n = b.list.get(i);
                    int mid = newState();
                    if (n instanceof Bracket.CharNode) {
                        char ch = ((Bracket.CharNode) n).chr;
                        int[] arr = negate(ch, ch);
                        addTransitionRange(start, mid, arr[0], arr[0]);
                        addTransitionRange(start, mid, arr[1], arr[1]);
                    }
                    else {//range
                        RangeNode rn = (RangeNode) n;
                        int[] arr = negate(rn.start, rn.end);
                        addTransitionRange(start, mid, arr[0], arr[0]);
                        addTransitionRange(start, mid, arr[1], arr[1]);
                    }
                    addEpsilon(mid, end);
                }
                p.end = end;
            }
            else {
                int end = newState();//order not matter?
                for (int i = 0; i < b.list.size(); i++) {
                    Node n = b.list.get(i);
                    int mid = newState();
                    if (n instanceof Bracket.CharNode) {
                        char ch = ((Bracket.CharNode) n).chr;
                        addTransitionRange(start, mid, ch, ch);
                    }
                    else {//range
                        RangeNode rn = (RangeNode) n;
                        addTransitionRange(start, mid, rn.start, rn.end);
                    }
                    addEpsilon(mid, end);
                }
                p.end = end;
            }
        }
        else if (node.is(StringNode.class)) {
            StringNode sn = (StringNode) node;
            if (sn.isDot) {
                p.end = insert(sn.toBracket(), start).end;
            }
            else {
                String str = sn.value;
                int st = start;
                int ns = start;
                for (char ch : str.toCharArray()) {
                    ns = newState();
                    addTransitionRange(st, ns, ch, ch);
                    st = ns;
                }
                p.end = ns;
            }

        }
        else if (node instanceof RegexNode) {
            RegexNode rn = (RegexNode) node;
            if (rn.star) {
                int ns = newState();
                addEpsilon(start, ns);//zero
                Pair st = insert(rn.node, start);
                addEpsilon(st.end, start);//repeat
                p.end = ns;
            }
            else if (rn.plus) {
                int ns = newState();
                addEpsilon(start, ns);
                Pair st = insert(rn.node, ns);
                addEpsilon(st.end, ns);//repeat
                p.end = ns;
            }
            else if (rn.optional) {
                int ns = newState();
                addEpsilon(start, ns);//zero times
                Pair st = insert(rn.node, start);
                addEpsilon(st.end, ns);
                p.end = ns;
            }

        }
        else if (node.is(OrNode.class)) {
            OrNode or = (OrNode) node;
            int end = newState();
            for (Node n : or.list.list) {
                int e = insert(n, start).end;
                addEpsilon(e, end);
            }
            p.end = end;
        }
        else if (node.is(GroupNode.class)) {
            GroupNode<Node> group = (GroupNode<Node>) node;
            Node rhs = group.rhs;
            p.end = insert(rhs, start).end;
        }
        else if (node.is(NameNode.class)) {//?
            NameNode name = (NameNode) node;
            p.end = insert(tree.getToken(name.name).regex, start).end;
        }
        return p;
    }

    int newState() {
        return ++numStates;
    }

    static int segment(int start, int end) {
        return (start << 16) | end;
    }

    int segment(int ch) {
        return segment(ch, ch);
    }

    //segment to code points range
    int[] desegment(int seg) {
        int end = seg & (1 << 16);
        int start = seg >> 16;
        return new int[]{start, end};
    }

    //insert regex token to initial state
    public void addRegex(Node node) {
        //more than one final states?
        Pair p = insert(node, initial);
        setAccepting(p.end, true);
    }

    public void dump(String path) throws IOException {
        PrintWriter w = new PrintWriter(System.out);

        for (int i = initial; i < numStates; i++) {
            w.println(i);
            Set<Integer> syms = inputMap.get(i);
            for (int s : syms) {
                StateSet targets = table[i][s];
                w.print("  ");
                w.print(decodeSegment(s));
                w.print(" -> ");
                for (int st : targets.states) {
                    w.print(st);
                    w.print(" ");
                }
                w.println();
            }
        }
    }

    public void dumpAlphabet() {
        /*for (int state = initial; state < numStates; state++) {
            Set<Integer> set = inputMap.get(state);
            for (int input : set) {
                System.out.println(decodeSegment(input));
            }
        }*/
        for (int x : alphabet.keySet()) {
            System.out.println(decodeSegment(x));
        }
    }

    String decodeSegment(int seg) {
        int[] arr = desegment(seg);
        return (char) arr[0] + "-" + (char) arr[1];
    }
}

