package mesut.parserx.nodes;

public class Epsilon extends Name {

    public Epsilon() {
        super(str());
    }

    static String str() {
        //return "\uD835\uDF74";//bold
        return "\u03B5";
    }

    @Override
    public String toString() {
        return str();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Epsilon;
    }

    @Override
    public boolean isName() {
        return false;
    }
}
