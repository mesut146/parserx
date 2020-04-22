package nodes;

//lexer rule without regex
public class StringNode extends Node {
    public String value;

    public StringNode() {
    }

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }


}
