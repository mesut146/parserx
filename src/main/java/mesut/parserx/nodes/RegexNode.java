package mesut.parserx.nodes;

import java.util.Objects;

public class RegexNode extends Node {

    public Node node;//lexer or parser rule
    public String type;

    public RegexNode(Node rule, String type) {
        if (rule.isSequence()) {
            rule = new GroupNode(rule);
        }
        this.node = rule;
        this.type = type;
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
            return new RegexNode(node.asRegex().node, "*");
        }
        return this;
    }

    @Override
    public Node copy() {
        return new RegexNode(node, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegexNode regexNode = (RegexNode) o;

        if (!Objects.equals(node, regexNode.node)) return false;
        return Objects.equals(type, regexNode.type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}

