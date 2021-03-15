package nodes;

public class RegexNode extends Node {

    public Node node;//lexer or parser rule
    String type;

    public RegexNode() {
    }

    public RegexNode(Node rule, String type) {
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
}

