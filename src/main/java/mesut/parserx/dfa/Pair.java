package mesut.parserx.dfa;

public class Pair {

    public int start;
    public int end;

    public Pair(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static Pair of(int start, int end) {
        return new Pair(start, end);
    }
}
