package nodes;

//base class used in grammar file
public class Node {

    public boolean isSequence() {
        return this instanceof Sequence;
    }

    public Sequence asSequence() {
        return (Sequence) this;
    }

    public boolean isRegex() {
        return this instanceof RegexNode;
    }

    public RegexNode asRegex() {
        return (RegexNode) this;
    }

    public boolean isString() {
        return this instanceof StringNode;
    }

    public StringNode asString() {
        return (StringNode) this;
    }


    public boolean isBracket() {
        return this instanceof Bracket;
    }

    public boolean isRange() {
        return this instanceof RangeNode;
    }

    public RangeNode asRange() {
        return (RangeNode) this;
    }

    public boolean isChar() {
        return this instanceof RangeNode;
    }

    public Bracket.CharNode asChar() {
        return (Bracket.CharNode) this;
    }

    public <T> boolean is(Class<T> c) {
        return getClass() == c;
    }

    public <T> T as(Class<T> c) {
        return (T) this;
    }
}
