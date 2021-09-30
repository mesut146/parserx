package mesut.parserx.nodes;

import mesut.parserx.gen.ll.AstInfo;

//base class used in grammar file
public abstract class Node {

    public String label;//name in alternation
    public AstInfo astInfo = new AstInfo();

    public boolean isSequence() {
        return this instanceof Sequence;
    }

    public Sequence asSequence() {
        return (Sequence) this;
    }

    public boolean isRegex() {
        return this instanceof Regex;
    }

    public Regex asRegex() {
        return (Regex) this;
    }

    public boolean isOr() {
        return this instanceof Or;
    }

    public Or asOr() {
        return (Or) this;
    }

    public boolean isGroup() {
        return this instanceof Group;
    }

    public Group asGroup() {
        return (Group) this;
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
        return this instanceof Range;
    }

    public Range asRange() {
        return (Range) this;
    }

    public boolean isName() {
        return this instanceof Name;
    }

    public Name asName() {
        return (Name) this;
    }

    public boolean isEpsilon() {
        return this instanceof Epsilon;
    }

    public boolean isOptional() {
        return isRegex() && asRegex().isOptional();
    }

    public boolean isStar() {
        return isRegex() && asRegex().isStar();
    }

    public boolean isPlus() {
        return isRegex() && asRegex().isPlus();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public Node copy() {
        return this;
    }

    public boolean isDot() {
        return this instanceof Dot;
    }

    protected String varString() {
        if (astInfo.varName != null) return astInfo.varName + " = ";
        return "";
    }

    String withLabel() {
        if (label == null) return toString();
        return toString() + " #" + label;
    }

    public Node normal() {
        return this;
    }
}
