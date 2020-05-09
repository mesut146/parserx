package dfa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//nfa state set
public class StateSet {

    public List<Integer> states;

    public StateSet() {
        states = new ArrayList<>();
    }

    public void addState(int state) {
        states.add(state);
    }

    public void addAll(StateSet other) {
        states.addAll(other.states);
    }

    public int get(int index) {
        return states.get(index);
    }

    public void remove(int state) {
        states.remove((Integer) state);
    }

    public void clear() {
        states.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateSet stateSet = (StateSet) o;
        return states.equals(stateSet.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }
}
