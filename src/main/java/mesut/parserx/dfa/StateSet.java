package mesut.parserx.dfa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//nfa,dfa state set
public class StateSet implements Iterable<Integer> {

    public Set<Integer> states = new HashSet<>();

    public void addState(int state) {
        states.add(state);
    }

    public void addAll(StateSet other) {
        states.addAll(other.states);
    }

    public boolean contains(int state) {
        return states.contains(state);
    }


    public void remove(int state) {
        states.remove(state);
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

    @Override
    public String toString() {
        return "StateSet{" + states + "}";
    }


}
