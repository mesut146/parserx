package mesut.parserx.nodes;

public class Regex extends Node {

    public Node node;
    public RegexType type;

    public Regex(Node node, RegexType type) {
        if (node.isSequence() || node.isOr()) {
            throw new RuntimeException("invalid child, wrap using group");
        }
        this.node = node;
        this.type = type;
        if (isStar() || isPlus()) {
            this.node.astInfo.isInLoop = true;
        }
    }

    public static Regex wrap(Node node, RegexType type) {
        if (node.isSequence() || node.isOr()) return new Regex(new Group(node), type);
        return new Regex(node, type);
    }

    public boolean isStar() {
        return type == RegexType.STAR;
    }

    public boolean isPlus() {
        return type == RegexType.PLUS;
    }

    public boolean isOptional() {
        return type == RegexType.OPTIONAL;
    }

    @Override
    public String toString() {
        if (astInfo.isFactored) {
            return node + type.toString() + "()";
        }
        return node + type.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var regex = (Regex) o;

        return node.equals(regex.node) && type.equals(regex.type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + node.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitRegex(this, arg);
    }

}

