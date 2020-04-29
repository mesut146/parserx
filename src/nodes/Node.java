package nodes;

//base class used in grammar file
public class Node {

    public Sequence asSequence() {
        return (Sequence) this;
    }

    public RegexNode asRegex() {
        return (RegexNode) this;
    }

    public StringNode asString() {
        return (StringNode) this;
    }
}
