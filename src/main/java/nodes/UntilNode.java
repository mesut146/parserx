package nodes;

public class UntilNode extends Node {
    Node node;

    public UntilNode(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "~" + node;
    }

    public Node transform() {
        //make normal regex

        return null;
    }
}