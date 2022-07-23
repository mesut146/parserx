package mesut.parserx.dfa;

import java.util.ArrayList;
import java.util.List;

public class State {
    public int state;
    public List<Transition> transitions = new ArrayList<>();
    public List<Transition> incoming = new ArrayList<>();
    public boolean isSkip = false;
    public boolean accepting = false;
    public List<String> names = new ArrayList<>();

    public State(int state) {
        this.state = state;
    }

    public void add(Transition tr) {
        if (transitions.contains(tr)) {
            return;
        }
        transitions.add(tr);
        tr.target.incoming.add(tr);
    }

    public void addEpsilon(State to) {
        add(new Transition(this, to));
    }

    public void addName(String name) {
        if (!names.contains(name)) {
            names.add(name);
        }
    }

    @Override
    public String toString() {
        return "" + state;
    }

    @Override
    public boolean equals(Object obj) {
        State other = (State) obj;
        return state == other.state;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(state);
    }
}
