package mesut.parserx.nodes;

//base class used in grammar file
public class Node {

    public String name;//variable name
    public String label;//name in alternation

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

    public GroupNode asGroup() {
        return (GroupNode) this;
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

    public boolean isName() {
        return this instanceof NameNode;
    }

    public NameNode asName() {
        return (NameNode) this;
    }

    public boolean isEmpty() {
        return this instanceof EmptyNode;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public Node copy() {
        return this;
    }

    public boolean isDot() {
        return this instanceof DotNode;
    }

    public DotNode asDot() {
        return (DotNode) this;
    }

    protected String varString() {
        if (name != null) return name + " = ";
        return "";
    }

    public Node normal() {
        return this;
    }
}
