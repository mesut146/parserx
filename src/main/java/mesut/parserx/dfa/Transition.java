package mesut.parserx.dfa;

import mesut.parserx.nodes.Epsilon;

import java.util.Objects;

//a single arrow
public class Transition {
    public static Alphabet alphabet;
    public int state;//from
    public int target;
    public int input;
    public boolean epsilon;

    public Transition(int state, int target, int input) {
        this.state = state;
        this.target = target;
        this.input = input;
    }

    public Transition(int state, int target) {
        this.state = state;
        this.target = target;
        this.epsilon = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return target == that.target &&
                input == that.input &&
                state == that.state &&
                epsilon == that.epsilon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, input, state, epsilon);
    }

    @Override
    public String toString() {
        return "Transition{" +
                "state=" + state +
                ", target=" + target +
                ", symbol=" + (epsilon ? Epsilon.str() : (alphabet != null ? alphabet.getRegex(input) : input)) +
                '}';
    }
}
