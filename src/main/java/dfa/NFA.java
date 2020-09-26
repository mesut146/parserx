package dfa;

import grammar.ParseException;
import nodes.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@SuppressWarnings("unchecked")
public class NFA {
    public static boolean debugTransition = false;
    public static boolean debugDFA = false;
    public Tree tree;//grammar file
    public int numStates;
    public int initial = 0;//initial state
    public List<Transition>[] trans;
    public HashMap<Integer, Integer> alphabet;//code point(segment) to index
    public int[] inputIndex;//index to segment
    public String[] names;
    boolean[] accepting;//[state]=isAccepting
    StateSet[] epsilon;//[state]=set of next states with epsilon moves
    Set<int[]> inputClasses;
    boolean[] isSkip;//if that final state is ignored

    public NFA(int numStates) {
        trans = new List[numStates];
        accepting = new boolean[numStates];
        epsilon = new StateSet[numStates];
        this.numStates = 0;//just initial
        alphabet = new HashMap<>();
        inputIndex = new int[255];
        names = new String[numStates];
        isSkip = new boolean[numStates];
        inputClasses = new HashSet<>();
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
        epsilon = expand(epsilon, newEpsilon, len);
        trans = expand(trans, newTrans, len);
        isSkip = expand(isSkip, newSkip, len);
        names = expand(names, newNames, len);
    }

    <T> T expand(Object src, Object target, int len) {
        System.arraycopy(src, 0, target, 0, len);
        return (T) target;
    }

    //state index,input index,target state index
    public void addTransition(int state, int target, int input) {
        expand(state);
        //System.out.printf("state: %d input: %d target: %d\n", state, input, target);
        List<Transition> arr;
        arr = trans[state];
        if (arr == null) {
            arr = new ArrayList<>();
        }
        trans[state] = arr;
        Transition tr = new Transition(state, input, target);
        arr.add(tr);
    }

    public void addTransitionRange(int state, int target, int left, int right) {
        //System.out.printf("st:%d (%c-%c) to st:%d nm:%s nm2:%s\n",state,left,right,target,names[state],names[target]);
        int seg = CharClass.segment(left, right);
        if (debugTransition)
            System.out.printf("st:%d (%s-%s) to st:%d seg:%d\n", state, CharClass.printChar(left), CharClass.printChar(right), target, seg);
        addTransition(state, target, seg);
    }

    public void setAccepting(int state, boolean val) {
        expand(state);
        accepting[state] = val;
    }

    public boolean isAccepting(int state) {
        return accepting[state];
    }

    public void addEpsilon(int state, int target) {
        expand(state);
        StateSet set = epsilon[state];
        if (set == null) {
            set = epsilon[state] = new StateSet();
        }
        set.addState(target);
    }

    //add regex to @start
    //return start,end state
    public Pair insert(Node node, int start) {
        Pair p = new Pair(start + 1, start + 1);
        if (node.isString()) {
            StringNode sn = (StringNode) node;
            if (sn.isDot) {
                p = insert(sn.toBracket(), start);
            }
            else {
                String str = sn.value;
                int st = start;
                int ns = start;
                p.start = numStates + 1;
                for (char ch : str.toCharArray()) {
                    ns = newState();
                    addTransitionRange(st, ns, ch, ch);
                    st = ns;
                }
                p.end = ns;
            }
        }
        else if (node.isBracket()) {
            Bracket b = node.asBracket();
            if (b.negate) {
                //todo all at once
                int end = newState();
                List<RangeNode> ranges = b.negateAll();
                for (RangeNode n : ranges) {
                    //int mid = newState();
                    addTransitionRange(start, end, n.start, n.end);
                    //addEpsilon(mid, end);
                }
                p.end = end;
            }
            else {//normal char range
                //in order to have only one end state we add epsilons?
                int end = newState();
                for (int i = 0; i < b.list.size(); i++) {
                    Node n = b.list.get(i);
                    //int mid = newState();
                    int left, right;
                    if (n instanceof Bracket.CharNode) {
                        left = right = ((Bracket.CharNode) n).chr;
                    }
                    else {//range
                        RangeNode rn = (RangeNode) n;
                        left = rn.start;
                        right = rn.end;
                    }
                    addTransitionRange(start, end, left, right);
                    //addEpsilon(mid, end);
                }
                p.end = end;
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st = start;
            for (Node child : seq.list) {
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
                //todo no mid state will cause error later
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
            for (Node n : or.list) {
                int e = insert(n, start).end;
                addEpsilon(e, end);//to have one end state
            }
            p.end = end;
        }
        else if (node.isGroup()) {
            GroupNode group = node.asGroup();
            Node rhs = group.rhs;
            p.end = insert(rhs, start).end;
        }
        else if (node.isName()) {//?
            //we have lexer ref just replace with target's regex
            NameNode name = (NameNode) node;
            p.end = insert(tree.getToken(name.name).regex, start).end;
        }
        return p;
    }

    int newState() {
        return ++numStates;
    }


    //insert regex token to initial state
    public void addRegex(TokenDecl decl) throws ParseException {
        Pair p = insert(decl.regex, initial);
        setAccepting(p.end, true);
        names[p.end] = decl.tokenName;
        isSkip[p.end] = decl.isSkip;
    }

    public void dump(String path) throws IOException {
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
            StateSet eps = epsilon[state];
            if (eps != null) {
                w.print("  E -> ");
                for (int e : eps.states) {
                    w.print(printState(e));
                    w.print(" ");
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

    public DFA dfa() {
        if (debugDFA)
            System.out.println("dfa conversion started");
        DFA dfa = new DFA(trans.length * 2);
        dfa.tree = tree;

        Map<StateSet, Integer> dfaStateMap = new HashMap<>();//state set to dfa state
        Queue<StateSet> openStates = new LinkedList<>();
        Set<StateSet> processed = new HashSet<>();
        openStates.add(closure(initial));
        dfa.numStates = -1;

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
                List<Transition> trList = trans[epState];
                if (trList != null) {
                    for (Transition t : trList) {
                        StateSet targetCLosure = closure(t.target);
                        StateSet targets = map.get(t.input);//we can cache these
                        if (targets == null) {
                            targets = new StateSet();
                            map.put(t.input, targets);
                        }
                        targets.addAll(targetCLosure);
                    }
                }
            }
            if (debugDFA)
                System.out.printf("map(%s)=%s\n", state, map);
            //make transition for each input
            for (int input : map.keySet()) {
                //String seg = CharClass.seg2escaped(input);
                StateSet targets = map.get(input);
                if (!openStates.contains(targets) && !processed.contains(targets)) {
                    openStates.add(targets);
                }
                int target_state = getDfaState(dfaStateMap, targets, dfa);
                if (debugDFA)
                    System.out.printf("targets=%s dfa=%d\n", targets, target_state);
                dfa.setAccepting(target_state, isAccepting(targets));
                dfa.addTransition(dfaState, input, target_state);
                dfa.isSkip[target_state] = isSkip(targets);
                dfa.names[target_state] = getName(targets);
            }
        }
        dfa.optimize();
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
    int getDfaState(Map<StateSet, Integer> map, StateSet set, DFA dfa) {
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
        StateSet eps = epsilon[state];
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
                StateSet eps = epsilon[state];
                if (list != null) {
                    for (Transition tr : list) {
                        w.printf("%s -> %s [label=\"[%s]\"]\n", state, tr.target, CharClass.seg2escaped(tr.input));
                    }
                }
                if (eps != null) {
                    for (int target : eps) {
                        w.printf("%s -> %s [label=\"ε\"]\n", state, target);
                    }
                }
            }
            w.println("}");
            w.flush();
            w.close();
            System.out.println("nfa dot file writed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

