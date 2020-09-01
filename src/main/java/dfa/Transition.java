package dfa;

//a single arrow
public class Transition {
    public int target;
    public int input;
    int state;//from
    int segment;

    public Transition(int state, int input, int target) {
        this.state = state;
        this.target = target;
        this.input = input;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "state=" + state +
                ", target=" + target +
                ", symbol=" + input +
                '}';
    }
}
