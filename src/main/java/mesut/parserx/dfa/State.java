package mesut.parserx.dfa;

import mesut.parserx.nodes.TokenDecl;

import java.util.ArrayList;
import java.util.List;

public class State {
    public int id;
    public List<Transition> transitions = new ArrayList<>();
    public List<Transition> incoming = new ArrayList<>();
    public boolean isSkip = false;
    public boolean accepting = false;
    public String name;
    public TokenDecl decl;
    public int which = -1;

    public State(int id) {
        this.id = id;
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


    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object obj) {
        State other = (State) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
