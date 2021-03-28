package mesut.parserx.nodes;

public class DotNode extends Node {

    public static DotNode instance = new DotNode();
    public static Bracket bracket = toBracket().normalize();

    //convert dot to bracket node
    public static Bracket toBracket() {
        Bracket b = new Bracket();
        b.add('\n');
        b.negate = true;
        return b;
    }

    public static Bracket dot2() {
        Bracket b = new Bracket();
        b.add('\n');
        b.add('\r');
        b.negate = true;
        return b;
    }

    @Override
    public String toString() {
        return ".";
    }
}
