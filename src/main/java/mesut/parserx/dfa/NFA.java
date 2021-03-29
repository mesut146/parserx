package mesut.parserx.dfa;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class NFA {
    public static boolean debugTransition = false;
    public static boolean debugDFA = false;
    public Tree tree;//grammar file
    public List<Transition>[] trans;
    public boolean[] accepting;
    public boolean[] isSkip;
    public String[] names;
    public int initial = 0;
    public int lastState = 0;

    public NFA(int maxStates, Tree tree) {
        trans = new List[maxStates];
        accepting = new boolean[maxStates];
        isSkip = new boolean[maxStates];
        names = new String[maxStates];
        this.tree = tree;
    }

    public NFA(int maxStates) {
        this(maxStates, new Tree());
    }

    public static NFA makeDFA(File path) {
        return Tree.makeTree(path).makeNFA().dfa();
    }

    public void expand(int state) {
        if (state < trans.length) {
            return;
        }
        int max = state * 2;
        int len = trans.length;

        boolean[] newAccepting = new boolean[max];
        boolean[] newSkip = new boolean[max];
        StateSet[] newEpsilon = new StateSet[max];
        List<Transition>[] newTrans = new List[max];
        String[] newNames = new String[max];
        accepting = expand(accepting, newAccepting, len);
        trans = expand(trans, newTrans, len);
        isSkip = expand(isSkip, newSkip, len);
        names = expand(names, newNames, len);
    }

    <T> T expand(Object src, Object target, int len) {
        System.arraycopy(src, 0, target, 0, len);
        return (T) target;
    }

    //epsilon transition targets from a state
    StateSet getEps(int state) {
        StateSet stateSet = new StateSet();
        if (hasTransitions(state)) {
            for (Transition tr : trans[state]) {
                if (tr.epsilon) {
                    stateSet.addState(tr.target);
                }
            }
        }
        return stateSet;
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
        add(new Transition(state, target, input));
        lastState = Math.max(lastState, Math.max(state, target));
    }

    private void add(Transition tr) {
        expand(Math.max(tr.state, tr.target));
        List<Transition> arr;
        arr = trans[tr.state];
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
        addTransition(state, target, getAlphabet().getId(RangeNode.of(left, right)));
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
            add(Transition.epsilon(state, target));
        }
    }

    public List<Transition> findIncoming(int to) {
        List<Transition> all = new ArrayList<>();
        for (int state = 0; state <= lastState; state++) {
            if (hasTransitions(state)) {
                List<Transition> list = trans[state];
                for (Transition transition : list) {
                    if (transition.target == to) {
                        all.add(transition);
                    }
                }
            }
        }
        return all;
    }

    //add regex to @start
    //return start,end state
    public Pair insert(Node node, int start) {
        Pair p = new Pair(start + 1, start + 1);
        if (node.isString()) {
            String str = node.asString().value;
            int st = start;
            int ns = start;
            p.start = lastState + 1;
            for (char ch : str.toCharArray()) {
                ns = newState();
                addTransitionRange(st, ns, ch, ch);
                st = ns;
            }
            p.end = ns;
        }
        else if (node.isDot()) {
            p = insert(DotNode.bracket, start);
        }
        else if (node.isBracket()) {
            Bracket b = node.asBracket().normalize();
            int end = newState();
            //in order to have only one end state we add epsilons?
            for (int i = 0; i < b.size(); i++) {
                RangeNode rn = b.rangeNodes.get(i);
                int left = rn.start;
                int right = rn.end;
                addTransitionRange(start, end, left, right);
            }
            p.end = end;
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st = start;
            for (Node child : seq) {
                st = insert(child, st).end;
            }
            p.end = st;
        }
        else if (node.isRegex()) {
            RegexNode rn = node.asRegex();
            if (rn.isStar()) {
                int end = newState();
                addEpsilon(start, end);//zero
                Pair st = insert(rn.node, start);
                addEpsilon(st.end, start);//repeat
                p.end = end;
            }
            else if (rn.isPlus()) {
                int newState = newState();
                addEpsilon(start, newState);
                Pair st = insert(rn.node, newState);
                addEpsilon(st.end, newState);//repeat
                p = st;
            }
            else if (rn.isOptional()) {
                int end = newState();
                addEpsilon(start, end);//zero times
                Pair st = insert(rn.node, start);
                addEpsilon(st.end, end);
                p.end = end;
            }
        }
        else if (node.isOr()) {
            OrNode or = (OrNode) node;
            int end = newState();
            for (Node n : or) {
                int e = insert(n, start).end;
                addEpsilon(e, end);//to have one end state
            }
            p.end = end;
        }
        else if (node.isGroup()) {
            GroupNode group = node.asGroup();
            Node rhs = group.node;
            p.end = insert(rhs, start).end;
        }
        else if (node.isName()) {//?
            //we have lexer ref just replace with target's regex
            NameNode name = node.asName();
            p.end = insert(tree.getToken(name.name).regex, start).end;
        }
        return p;
    }

    public int newState() {
        return ++lastState;
    }

    //insert regex token to initial state
    public void addRegex(TokenDecl decl) {
        Pair p = insert(decl.regex, initial);
        setAccepting(p.end, true);
        names[p.end] = decl.tokenName;
        isSkip[p.end] = decl.isSkip;
    }

    public void dump(PrintWriter w) {
        for (int state = 0; state <= lastState; state++) {
            if (hasTransitions(state)) {
                List<Transition> arr = trans[state];
                w.println(printState(state));
                sort(arr);
                for (Transition tr : arr) {
                    w.print("  ");
                    if (tr.epsilon) {
                        w.print("E");
                    }
                    else {
                        w.print(getAlphabet().getRegex(tr.input));
                    }

                    w.print(" -> ");
                    w.print(printState(tr.target));
                    w.println();
                }
            }
            w.println();
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

    String printState(int st) {
        if (isAccepting(st)) {
            return "(S" + st + (names[st] == null ? "" : ", " + names[st]) + ")";
        }
        return "S" + st;
    }

    public NFA dfa() {
        if (debugDFA)
            System.out.println("dfa conversion started");
        NFA dfa = new NFA(trans.length * 2);
        dfa.tree = tree;

        Map<StateSet, Integer> dfaStateMap = new HashMap<>();//state set to dfa state
        Queue<StateSet> openStates = new LinkedList<>();
        Set<StateSet> processed = new HashSet<>();
        openStates.add(closure(initial));
        dfa.lastState = -1;

        while (!openStates.isEmpty()) {
            StateSet state = openStates.poll();//current nfa state set
            StateSet closure = closure(state);//1,2,3
            /*if (closureMap.containsKey(state)) {
                List<StateSet> list = closureMap.get(state);
            }*/
            //closureMap.put(state, closure);
            //openStates.addAll(closure.states);
            //processed.addAll(closure.states);
            processed.add(state);
            //openStates.removeAll(closure.states);

            //get corresponding dfa state
            int dfaState = getDfaState(dfaStateMap, closure, dfa);
            if (debugDFA)
                System.out.printf("Closure(%s)=%s dfa=%d\n", state, closure, dfaState);
            //Set<Integer> inputSet = new HashSet<>();//0,1
            Map<Integer, StateSet> map = new HashMap<>();//input -> target states from state
            //find inputs and target states from state
            for (int epState : closure) {
                if (!hasTransitions(epState)) {
                    continue;
                }
                for (Transition t : trans[epState]) {
                    StateSet targetCLosure = closure(t.target);
                    StateSet targets = map.get(t.input);//we can cache these
                    if (targets == null) {
                        targets = new StateSet();
                        map.put(t.input, targets);
                    }
                    targets.addAll(targetCLosure);
                }
            }
            if (debugDFA)
                System.out.printf("map(%s)=%s\n", state, map);
            //make transition for each input
            for (int input : map.keySet()) {
                StateSet targets = map.get(input);
                if (!openStates.contains(targets) && !processed.contains(targets)) {
                    openStates.add(targets);
                }
                int target_state = getDfaState(dfaStateMap, targets, dfa);
                if (debugDFA)
                    System.out.printf("targets=%s dfa=%d\n", targets, target_state);
                dfa.setAccepting(target_state, isAccepting(targets));
                dfa.addTransition(dfaState, target_state, input);
                dfa.isSkip[target_state] = isSkip(targets);
                dfa.names[target_state] = getName(targets);
            }
        }
        return dfa;
    }

    boolean isAccepting(StateSet set) {
        for (int state : set) {
            if (isAccepting(state)) {
                return true;
            }
        }
        return false;
    }

    boolean isSkip(StateSet set) {
        for (int state : set) {
            if (isSkip[state]) {
                return true;
            }
        }
        return false;
    }

    //get token name for state set as defined order
    String getName(StateSet set) {
        int minIndex = Integer.MAX_VALUE;
        String name = null;
        for (int state : set) {
            if (names[state] != null) {
                int i = tree.indexOf(names[state]);
                if (i < minIndex) {
                    name = names[state];
                    minIndex = i;
                }
            }
        }
        return name;
    }

    //make dfa state for nfa state @set
    int getDfaState(Map<StateSet, Integer> map, StateSet set, NFA dfa) {
        Integer state = map.get(set);
        if (state == null) {
            state = dfa.newState();
            map.put(set, state);
        }
        return state;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(int state) {
        StateSet res = new StateSet();
        res.addState(state);//itself
        StateSet eps = getEps(state);
        if (eps == null || eps.states.size() == 0) {
            return res;
        }
        for (int st : eps.states) {
            if (!res.contains(st)) {//prevent stack overflow
                res.addAll(closure(st));
            }
        }
        return res;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(StateSet set) {
        StateSet res = new StateSet();
        for (int state : set) {
            res.addAll(closure(state));
        }
        return res;
    }

    public void dot(Writer writer) {
        PrintWriter w = new PrintWriter(writer);
        w.println("digraph G{");
        w.println("rankdir = LR");
        w.printf("%d [color=red]\n", initial);
        for (int state = 0; state <= lastState; state++) {
            if (isAccepting(state)) {
                w.printf("%d [shape = doublecircle xlabel=\"%s\"]\n", state, names[state]);
            }
            if (isSkip[state]) {
                w.printf("%d [color=blue xlabel=\"%s\"]\n", state, names[state]);
            }
        }

        for (int state = 0; state <= lastState; state++) {
            List<Transition> list = trans[state];
            StateSet eps = getEps(state);
            if (list != null) {
                for (Transition tr : list) {
                    w.printf("%s -> %s [label=\"[%s]\"]\n", state, tr.target, UnicodeUtils.escapeString(getAlphabet().getRegex(tr.input).toString()));
                }
            }
            if (eps != null) {
                for (int target : eps) {
                    w.printf("%s -> %s [label=\"ε\"]\n", state, target);
                }
            }
        }
        w.println("}");
        w.close();
    }
}

