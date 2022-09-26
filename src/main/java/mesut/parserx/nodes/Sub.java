package mesut.parserx.nodes;

//regex - "abc" -> match regex except abc
public class Sub extends Node {
    public Node node;
    public StringNode string;

    public Sub(Node node, StringNode string) {
        this.node = node;
        this.string = string;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitSub(this, arg);
    }
}
