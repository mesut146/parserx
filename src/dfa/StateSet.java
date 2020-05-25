package dfa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//nfa state set
public class StateSet implements Iterable<Integer> {

    public Set<Integer> states;

    public StateSet() {
        states = new HashSet<>();
    }

    public void addState(int state) {
        states.add(state);
    }

    public void addAll(StateSet other) {
        states.addAll(other.states);
    }

    /*public int get(int index) {
        return states.get(index);
    }*/

    public void remove(int state) {
        states.remove((Integer) state);
    }

    public void clear() {
        states.clear();
    }

    @Override
    public Iterator<Integer> iterator() {
        return states.iterator();
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
        return states.hashCode();
    }
}
