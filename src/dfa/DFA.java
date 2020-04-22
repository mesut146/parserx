package dfa;

import nodes.Bracket;
import nodes.Node;
import nodes.StringNode;
import rule.Sequence;

import java.util.ArrayList;
import java.util.List;

public class DFA {
    DFAState q0;//initial state
    List<DFAState> all = new ArrayList<>();

    public DFA() {
        q0 = new DFAState(0);
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
