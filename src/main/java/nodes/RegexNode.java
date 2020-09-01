package nodes;

public class RegexNode extends Node {

    public Node node;//lexer node or parser rule
    public boolean star = false;
    public boolean plus = false;
    public boolean optional = false;

    public RegexNode() {
    }

    public RegexNode(Node rule) {
        this.node = rule;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(node);
        if (star) {
            sb.append("*");
        }
        else if (plus) {
            sb.append("+");
        }
        else if (optional) {
            sb.append("?");
        }
        return sb.toString();
    }
}

