package mesut.parserx.nodes;

public class Regex extends Node {

    public Node node;
    public String type;

    public Regex(Node rule, String type) {
        if (rule.isSequence()) {
            rule = new Group(rule);
        }
        this.node = rule;
        setType(type);
    }

    public boolean isStar() {
        return type.equals("*");
    }

    public boolean isPlus() {
        return type.equals("+");
    }

    public boolean isOptional() {
        return type.equals("?");
    }

    public void setType(String type) {
        if (!"+*?".contains(type)) {
            throw new RuntimeException("invalid regex type: " + type);
        }
        this.type = type;
    }

    @Override
    public String toString() {
        if (node.isSequence() || node.isOr()) {
            return "(" + node + ")" + type;
        }
        return node + type;
    }

    public Node normal() {
        if (isOptional() && node.isRegex() && node.asRegex().isPlus()) {
            return new Regex(node.asRegex().node, "*");
        }
        return this;
    }

    @Override
    public Regex copy() {
        return new Regex(node, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Regex regex = (Regex) o;

        return node.equals(regex.node) && type.equals(regex.type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + node.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

