package dfa;

import nodes.*;

import java.util.Iterator;

public class NFA {
    //[curState][input]=nextStateSet
    StateSet[][] table;
    boolean[] accepting;
    //[curState][nextState]=isEpsilon
    //boolean[][] epsilon;
    //[curState]=set to epsilon moves
    StateSet[] epsilon;
    int stateCount = 1;
    int numStates;
    int numInput;//alphabet
    int initial = 0;

    public NFA(int numStates) {
        table = new StateSet[numStates][255];
        accepting = new boolean[numStates];
        //epsilon = new boolean[numStates][numStates];
        epsilon = new StateSet[numStates];
    }

    public void expand(int max) {
        if (numStates >= max) {
            return;
        }
        StateSet[][] newTable = new StateSet[max][numInput];
        boolean[] newAccepting = new boolean[max];
        StateSet[] newEpsilon = new StateSet[max];
        System.arraycopy(table, 0, newTable, 0, numStates);
        System.arraycopy(accepting, 0, newAccepting, 0, numStates);
        System.arraycopy(epsilon, 0, newEpsilon, 0, numStates);
        table = newTable;
        accepting = newAccepting;
        epsilon = newEpsilon;
    }

    public void addTransition(int state, int input, int target) {
        StateSet set = table[state][input];
        if (set == null) {
            set = new StateSet();
            table[state][input] = set;
        }
        set.addState(target);
    }

    public void addTransition(int state, int input, StateSet targets) {
        StateSet set = table[state][input];
        if (set == null) {
            set = new StateSet();
            table[state][input] = set;
        }
        for (int target : targets.states) {
            set.addState(target);
        }
    }

    public void addTransition(StateSet states, int input, int target) {
        for (int state : states.states) {
            addTransition(state, input, target);
        }
    }

    public StateSet getTransition(int state, int input) {
        return table[state][input];
    }

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
        DFA dfa = new DFA(table.length * 2, 255);

        return dfa;
    }

    //add regex to @start
    //return start,end state
    //todo fix accepting
    public Pair insert(Node node, int start) {
        Pair p = new Pair(start + 1, start + 1);
        if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st=start;
            for (Node child : seq.list) {
                st = insert(child, st).end;
            }
            p.end=st;
        }
        else if (node.is(Bracket.class)) {
            Bracket b = node.as(Bracket.class);
            if (b.negate) {
                //todo complement
                int end=newState();//order not matter?
                //todo or these not concat
                for (int i = 0; i < b.list.size(); i++) {
                    Node n = b.list.get(i);
                    int mid=newState();
                    if (n instanceof Bracket.CharNode) {
                        char ch = ((Bracket.CharNode) n).chr;
                        int[] arr=negate(ch,ch);
                        addTransition(start, arr[0], mid);
                        addTransition(start, arr[1], mid);
                    }
                    else {//range
                        RangeNode rn = (RangeNode) n;
                        int[] arr=negate(rn.start,rn.end);
                        addTransition(start,arr[0],mid);
                        addTransition(start,arr[1],mid);
                    }
                    addEpsilon(mid,end);
                }
                p.end=end;
            }
            else {
                
                int end=newState();//order not matter?
                //todo or these not concat
                for (int i = 0; i < b.list.size(); i++) {
                    Node n = b.list.get(i);
                    int mid=newState();
                    if (n instanceof Bracket.CharNode) {
                        char ch = ((Bracket.CharNode) n).chr;
                        addTransition(start, segment(ch), mid);
                    }
                    else {//range
                        RangeNode rn = (RangeNode) n;
                        addTransition(start,segment(rn.start,rn.end),mid);
                    }
                    addEpsilon(mid,end);
                }
                p.end=end;
            }
        }
        else if (node.is(StringNode.class)) {
            StringNode sn = (StringNode) node;
            String str = sn.value;
            int st = start;
            int ns=start;
            for (char ch : str.toCharArray()) {
                ns=newState();
                addTransition(st, segment(ch), ns);
                st = ns;
            }
            p.end = ns;
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
            else if (rn.plus) {//todo read line of fil
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

        }else if(node.is(OrNode.class)){
            OrNode or=(OrNode)node;
            int end=newState();
            for(Node n:or.list.list){
                int e=insert(n,start).end;
                addEpsilon(e,end);
            }
            p.end=end;
        }else if(node.is(GroupNode.class)){
            GroupNode group=(GroupNode)node;
            Node rhs=group.rhs;
            int end=insert(rhs,start).end;
            p.end=end;
        }
        return p;
    }

    int newState() {
        return ++numStates;
    }
    
    int segment(int start,int end){
        return (start<<16)|end;
    }
    
    int segment(int ch){
        return segment(ch,ch);
    }
    
    int[] decode(int seg){
        int end=seg&(1<<16);
        int start=seg>>16;
        return new int[]{start,end};
    }
    
    //negate char range
    int[] negate(int start,int end){
        int l=segment(CharClass.min,start-1);
        int r=segment(end+1,CharClass.max);
        return new int[]{l,r};
    }

    //epsilon closure for dfa conversion
    StateSet closure(int state) {
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

    //insert regex token to initial state
    public void addRegex(Node node) {
        //more than one final states?
        Pair p = insert(node, initial);
        setAccepting(p.end, true);
    }
}

