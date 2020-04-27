package dfa;

import dfa.*;
import nodes.*;
import rule.*;
import java.util.*;

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
    int initial=0;

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
        StateSet[] newEpsilon=new StateSet[max];
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
        StateSet set=epsilon[state];
        if (set == null){
            set = epsilon[state] = new StateSet();
        }
        set.addState(target);
    }

    public DFA dfa() {
        DFA dfa = new DFA(table.length * 2, 255);

        return dfa;
    }

    //add regex to @start
    //return start,end state
    //todo fix accepting
    public Pair insert(Node node, int start) {
        Pair p=new Pair(start + 1, start + 1);
        if (node instanceof Sequence) {
            Sequence seq=(Sequence)node;

            for (Node child:seq.list.list){
                p = insert(child, p.end);
            }
        }
        else if (node instanceof Bracket) {
            Bracket b=(Bracket)node;
            if (b.negate){
                //todo complement
            }else{
                int st=start;
                //todo or these not concat
                for (int i=0;i < b.list.size();i++){
                    Node n=b.list.get(i);
                    if (n instanceof Bracket.CharNode){
                        char c=((Bracket.CharNode)n).chr;
                        numStates++;
                        addTransition(st, c, numStates);
                    }else{//range
                        RangeNode rn=(RangeNode)n;
                        Iterator<Character> it=rn.iterator();
                        while (it.hasNext()){
                            char c=it.next();
                            numStates++;
                            addTransition(st, c, numStates);
                            st = numStates;
                        }
                    }
                }
            }
        }
        else if (node instanceof StringNode) {
            StringNode sn=(StringNode)node;
            String str=sn.value;
            int st=start;
            p.start = numStates + 1;
            for (char c:str.toCharArray()){
                numStates++;
                addTransition(st, c, numStates);
                st = numStates;
            }
            p.end = st;
        }
        else if (node instanceof RegexNode){
            RegexNode rn=(RegexNode)node;
            if (rn.star){
                int ns=++numStates;
                addEpsilon(start, ns);//empty
                Pair st=insert(rn.node, ns);
                addEpsilon(st.end, ns);//repeat
                p.end=ns;
            }
            else if (rn.plus){
                Pair st=insert(rn.node, start);
                addEpsilon(st.end, st.start);//repeat
                p.end=st.end;
            }
            else if(rn.optional){
                int ns=++numStates;
                addEpsilon(start, ns);//empty
                Pair st=insert(rn.node, ns);
                addEpsilon(st.end,++numStates);
                addEpsilon(ns,numStates);
                p.end=numStates;
            }

        }
        return p;
    }

    StateSet closure(int state){
        StateSet res=new StateSet();
        res.addState(state);
        StateSet s=epsilon[state];
        if (s == null || s.states.size() == 0){
            return res;
        }
        //res.addAll(s);
        for (int i:s.states){
            res.addAll(closure(i));
        }
        return res;
    }

    public void addRegex(Node node){
        //more than one final states?
        Pair p=insert(node, initial);
        setAccepting(p.end, true);
    }
}

