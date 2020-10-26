package nodes;

public abstract class Transformer {
    public abstract Node transform(Node node);

    public Node transformGroup(GroupNode node) {
        return node;
    }
}
