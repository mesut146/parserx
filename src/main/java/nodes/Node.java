package nodes;

import rule.NameNode;

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

    public boolean isOr() {
        return this instanceof OrNode;
    }

    public OrNode asOr() {
        return (OrNode) this;
    }

    public boolean isGroup() {
        return this instanceof GroupNode;
    }

    public GroupNode<Node> asGroup() {
        return (GroupNode<Node>) this;
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

    public Bracket asBracket() {
        return (Bracket) this;
    }

    public boolean isRange() {
        return this instanceof RangeNode;
    }

    public RangeNode asRange() {
        return (RangeNode) this;
    }

    public boolean isChar() {
        return this instanceof Bracket.CharNode;
    }

    public Bracket.CharNode asChar() {
        return (Bracket.CharNode) this;
    }

    public boolean isName() {
        return this instanceof NameNode;
    }

    public NameNode asName() {
        return (NameNode) this;
    }

    public <T> boolean is(Class<T> c) {
        return getClass() == c;
    }

    public <T> T as(Class<T> c) {
        return (T) this;
    }
}