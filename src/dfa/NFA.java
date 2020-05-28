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
    public int initial = 0;//initial state
    //public List<List<Transition>> trans;//state,input,targets
    public List<Transition>[] trans;
    public HashMap<Integer, Integer> alphabet;//code point(segment) to index
    public int[] inputIndex;//index to segment
    //public List<List<Integer>> inputMap;//state to input set
    //public List<List<Integer>> transMap;//state to target state set
    public String[] names;


    public NFA(int numStates) {
        //table = new StateSet[numStates][255];
        trans = new List[numStates];
        accepting = new boolean[numStates];
        epsilon = new StateSet[numStates];
        this.numStates = 0;//just initial
        this.numInput = 0;
        alphabet = new HashMap<>();
        inputIndex = new int[255];
        //inputMap = new ArrayList<>();
        //transMap = new ArrayList<>();
        names = new String[100];
    }

    public void expand(int state) {
        if (state < trans.length) {
            return;
        }
        int max = state * 2;
        int len = trans.length;

        boolean[] newAccepting = new boolean[max];
        StateSet[] newEpsilon = new StateSet[max];
        List<Transition>[] newTrans = new List[max];
        System.arraycopy(accepting, 0, newAccepting, 0, len);
        System.arraycopy(epsilon, 0, newEpsilon, 0, len);
        System.arraycopy(trans, 0, newTrans, 0, len);
        accepting = newAccepting;
        epsilon = newEpsilon;
        trans = newTrans;
    }

    //too slow
    int getSegment(int index) {
        for (Map.Entry<Integer, Integer> e : alphabet.entrySet()) {
            if (e.getValue() == index) {
                return e.getKey();
            }
        }
        return -2;//no segment,not possible
    }

    //convert code point(segment) to index
    int checkInput(int segment) {
        if (segment == -1) {
            //System.out.println("aaa");
        }
        Integer index = alphabet.get(segment);
        if (index == null) {
            //starts from 1
            numInput++;
            index = numInput;
            alphabet.put(segment, index);
        }
        return index;
    }

    /*void addInputMap(int state, int input) {
        List<Integer> s;
        if (inputMap.size() < state) {
            s = new ArrayList<>();
            inputMap.add(state, s);
        }
        else {
            s = inputMap.get(state);
        }
        s.add(input);
    }*/

    /*void addTransMap(int state, int target) {
        List<Integer> s;
        if (transMap.size() < state) {
            s = new ArrayList<>();
            transMap.add(state, s);
        }
        else {
            s = transMap.get(state);
        }
        s.add(target);
    }*/


    //state index,input index,target state index
    public void addTransition(int state, int input, int target) {
        //addInputMap(state, input);
        //addTransMap(state, target);
        expand(state);
        //System.out.printf("state: %d input: %d target: %d\n", state, input, target);
        List<Transition> arr;
        arr = trans[state];
        if (arr == null || arr.get(0).state != state) {
            arr = new ArrayList<>();
        }
        trans[state] = arr;
        Transition tr = new Transition(state, input, target);
        arr.add(tr);
    }

    public void addTransitionRange(int state, int target, int left, int right) {
        //System.out.printf("st:%d (%c-%c) to st:%d nm:%s nm2:%s\n",state,left,right,target,names[state],names[target]);
        int seg = CharClass.segment(left, right);
        System.out.printf("st:%d (%s-%s) to st:%d seg:%d\n", state, CharClass.printChar(left), CharClass.printChar(right), target, seg);
        //int inputIndex = checkInput(seg);
        addTransition(state, seg, target);
        //addTransition(state, inputIndex, target);
    }

    public void setAccepting(int state, boolean val) {
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
        if (node.is(StringNode.class)) {
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
        else if (node.is(Bracket.class)) {
            Bracket b = node.as(Bracket.class);
            if (b.negate) {
                //todo all at once
                int end = newState();//order not matter?
                List<RangeNode> ranges = b.negateAll();
                for (int i = 0; i < ranges.size(); i++) {
                    RangeNode n = ranges.get(i);
                    //System.out.println(n);
                    int mid = newState();
                    addTransitionRange(start, end, n.start, n.end);
                    addEpsilon(mid, end);
                }
                p.end = end;
            }
            else {
                //in order to have only one end state we add epsilons
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
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st = start;
            for (Node child : seq.list) {
                st = insert(child, st).end;
            }
            p.end = st;
        }
        else if (node instanceof RegexNode) {
            RegexNode rn = (RegexNode) node;
            if (rn.star) {
                int end = newState();
                addEpsilon(start, end);//zero
                Pair st = insert(rn.node, end);
                addEpsilon(st.end, start);//repeat
                p.end = end;
            }
            else if (rn.plus) {
                int end = newState();
                addEpsilon(start, end);
                Pair st = insert(rn.node, end);
                addEpsilon(st.end, end);//repeat
                p.end = end;
            }
            else if (rn.optional) {
                int end = newState();
                addEpsilon(start, end);//zero times
                Pair st = insert(rn.node, start);
                addEpsilon(st.end, end);
                p.end = end;
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
        else if (node.isGroup()) {
            GroupNode<Node> group = node.asGroup();
            Node rhs = group.rhs;
            p.end = insert(rhs, start).end;
        }
        else if (node.is(NameNode.class)) {//?
            //we have lexer ref just replace target's regex
            NameNode name = (NameNode) node;
            p.end = insert(tree.getToken(name.name).regex, start).end;
        }
        return p;
    }

    int newState() {
        return ++numStates;
    }


    //insert regex token to initial state
    public void addRegex(TokenDecl decl) {
        //more than one final state?
        Pair p = insert(decl.regex, initial);
        //names[p.end] = decl.tokenName;
        setAccepting(p.end, true);
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
                    w.print(CharClass.seg2str(tr.symbol));

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

    /*public void dumpAlphabet() {
        for (int state = initial; state < numStates; state++) {
            Set<Integer> set = inputMap.get(state);
            for (int input : set) {
                System.out.println(decodeSegment(input));
            }
        }
        for (
                int x : alphabet.keySet()) {
            System.out.println(decodeSegment(x));
        }
    }*/
    //todo
    public DFA dfa() {
        DFA dfa = new DFA(trans.length * 2, numInput);
        //Map<StateSet, Integer> map = new HashMap<>();
        int init = 0;
        Map<StateSet, Integer> dfaStateMap = new HashMap<>();//state set to dfa state
        Queue<Integer> openStates = new LinkedList<>();
        Set<Integer> processed = new HashSet<>();
        openStates.add(initial);
        dfa.numStates = -1;
        while (!openStates.isEmpty()) {
            int state = openStates.poll();
            StateSet closure = closure(state);//1,2,3
            //openStates.addAll(closure.states);
            //processed.addAll(closure.states);
            processed.add(state);
            int dfaState = getDfaState(dfaStateMap, closure, dfa);//corresponding dfa state for closure

            System.out.printf("nfa state=%d dfa state=%d\n", state, dfaState);
            System.out.printf("Closure(%d)=%s\n", state, closure);
            //Set<Integer> inputSet = new HashSet<>();//0,1
            Map<Integer, StateSet> map = new HashMap<>();//input -> target states from state
            //find inputs and target states from state
            for (int epState : closure) {
                List<Transition> trList = trans[epState];
                for (Transition t : trList) {
                    if (!processed.contains(t.target)) {
                        openStates.offer(t.target);
                    }
                    StateSet targets = map.get(t.symbol);//we can cache these
                    if (targets == null) {
                        targets = new StateSet();
                        map.put(t.symbol, targets);
                    }
                    targets.addState(t.target);
                }
            }
            System.out.printf("map(%d)=%s\n", state, map);
            //for each input make transition
            for (int input : map.keySet()) {
                StateSet targets = map.get(input);
                int targets_state = getDfaState(dfaStateMap, targets, dfa);
                //dfa state for state
                dfa.addTransition(dfaState/*?*/, input, targets_state);
            }
        }
        return dfa;
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
        for (int i : eps.states) {
            res.addAll(closure(i));
        }
        return res;
    }
}

