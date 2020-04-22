package nodes;

public class RangeNode extends Node {

    public char start;
    public char end;

    public RangeNode(String s1, String s2) {
        start = s1.charAt(0);
        end = s2.charAt(0);
    }

    public RangeNode(char start, char end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }


}
