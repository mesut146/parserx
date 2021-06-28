package mesut.parserx.nodes;

public class Epsilon extends Name {

    public Epsilon() {
        super("\uD835\uDF74");
    }

    @Override
    public String toString() {
        return "\uD835\uDF74";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Epsilon;
    }
}
