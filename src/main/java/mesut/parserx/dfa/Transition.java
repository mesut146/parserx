package mesut.parserx.dfa;

import mesut.parserx.nodes.Epsilon;

import java.util.Objects;

//a single arrow
public class Transition {
    public static Alphabet alphabet;
    public State from;
    public State target;
    public int input;
    public boolean epsilon;

    public Transition(State from, State target, int input) {
        this.from = from;
        this.target = target;
        this.input = input;
    }

    public Transition(State from, State target) {
        this.from = from;
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
                from == that.from &&
                epsilon == that.epsilon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, input, from, epsilon);
    }

    @Override
    public String toString() {
        return "Transition{" +
                "state=" + from +
                ", target=" + target +
                ", symbol=" + (epsilon ? Epsilon.str() : (alphabet != null ? alphabet.getRegex(input) : input)) +
                '}';
    }
}
