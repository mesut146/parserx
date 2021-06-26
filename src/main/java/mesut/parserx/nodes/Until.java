package mesut.parserx.nodes;

public class Until extends Node {
    public Node node;

    public Until(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "~" + node;
    }

}
