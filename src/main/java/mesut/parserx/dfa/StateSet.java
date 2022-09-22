package mesut.parserx.dfa;

import java.util.*;

//nfa,dfa state set
public class StateSet implements Iterable<State> {

    public Set<State> states = new TreeSet<>(Comparator.comparingInt(o -> o.id));

    public void addState(State state) {
        states.add(state);
    }

    public void addAll(StateSet other) {
        states.addAll(other.states);
    }

    public boolean contains(State state) {
        return states.contains(state);
    }

    public void remove(State state) {
        states.remove(state);
    }

    @Override
    public Iterator<State> iterator() {
        return states.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var stateSet = (StateSet) o;
        return states.equals(stateSet.states);
    }

    @Override
    public int hashCode() {
        return states.hashCode();
    }

    @Override
    public String toString() {
        return "StateSet{" + states + "}";
    }


}
