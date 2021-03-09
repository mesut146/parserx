package dfa;

import nodes.Tree;

import java.io.File;

//each dfa is a nfa
public class DFA extends NFA {

    public DFA(int maxStates) {
        super(maxStates);
    }

    public static DFA makeDFA(File path) {
        return Tree.makeTree(path).makeNFA().dfa();
    }

    @Override
    public void addEpsilon(int state, int target) {
        throw new RuntimeException("epsilon in dfa");
    }
}
