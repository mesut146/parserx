package dfa;

import nodes.RangeNode;

import java.util.ArrayList;
import java.util.List;

public class DFAState {

    public int id;
    public char chr;
    public RangeNode range;
    public boolean accepting = false;
    public List<DFAState> states = new ArrayList<>();

    public DFAState() {
    }

    public DFAState(int id) {
        this.id = id;
    }
}
