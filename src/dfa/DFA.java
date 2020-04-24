package dfa;

import nodes.Bracket;
import nodes.Node;
import nodes.StringNode;
import rule.Sequence;

import java.util.ArrayList;
import java.util.List;

public class DFA {
    //[curState][inputChar]=nextState
    public int[][] table;
    public boolean[] accepting;

    public DFA() {
        this(10,255);
    }
    
    public DFA(int numStates,int numInput) {
        table = new int[numStates][numInput];
        accepting = new boolean[numStates];
    }
    
    public void addTransition(int state, int input, int target){
        table[state][input] = target;
    }

    public int getTransition(int state, int input){
        return table[state][input];
    }

    public void setAccepting(int state, boolean val){
        accepting[state] = val;
    }

    public boolean isAccepting(int state){
        return accepting[state];
    }

    public void insert(Node node) {
        if (node instanceof Sequence) {

        }
        else if (node instanceof Bracket) {

        }
        else if (node instanceof StringNode) {

        }
    }
}
