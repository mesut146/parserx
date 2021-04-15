package mesut.parserx.nodes;

public class UntilNode extends Node {
    public Node node;

    public UntilNode(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "~" + node;
    }

}
