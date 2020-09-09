package nodes;

public class RegexNode extends Node {

    public Node node;//lexer node or parser rule
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
        this.type = type;
    }

    @Override
    public String toString() {
        return node + type;
    }
}

