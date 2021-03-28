package mesut.parserx.dfa;

import java.util.Objects;

//a single arrow
public class Transition {
    public int state;//from
    public int target;
    public int input;
    public boolean epsilon;
    public static Alphabet alphabet;

    public Transition(int state, int target, int input) {
        this.state = state;
        this.target = target;
        this.input = input;
    }

    private Transition(int state, int target, boolean epsilon) {
        this.state = state;
        this.target = target;
        this.epsilon = epsilon;
    }

    public static Transition epsilon(int state, int target) {
        return new Transition(state, target, true);
    }

    public static Transition from(int from) {
        return new Transition(from, 0, 0);
    }

    public Transition to(int to) {
        this.target = to;
        return this;
    }

    public Transition by(int by) {
        this.input = by;
        return this;
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
                ", symbol=" + (epsilon ? "E" : (alphabet != null ? alphabet.getRegex(input) : input)) +
                '}';
    }
}
