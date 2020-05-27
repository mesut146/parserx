package dfa;

//a single arrow
public class Transition {
    int state;//from
    int target;
    int symbol;
    int segment;

    public Transition(int state, int input, int target) {
        this.state = state;
        this.target = target;
        this.symbol = input;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "state=" + state +
                ", target=" + target +
                ", symbol=" + symbol +
                '}';
    }
}
