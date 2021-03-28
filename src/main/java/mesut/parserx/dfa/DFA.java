package mesut.parserx.dfa;

import mesut.parserx.nodes.Tree;

import java.io.File;

//each dfa is a nfa
public class DFA extends NFA {

    public DFA(int maxStates) {
        super(maxStates);
    }

    public static DFA makeDFA(File path) {
        return Tree.makeTree(path).makeNFA().dfa();
    }
}
