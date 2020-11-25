package nodes;

public class Repetition extends Node {
    Node node;
    int min;
    int num;

    public Repetition(Node node, int num) {
        this.node = node;
        this.num = num;
    }

    @Override
    public String toString() {
        return node + "{" + num + "}";
    }
}
