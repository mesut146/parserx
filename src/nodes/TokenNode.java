package nodes;

public class TokenNode extends Node {
    public String name;

    public TokenNode(String name) {
        this.name = name;
    }

    public TokenNode() {
    }


    @Override
    public String toString() {
        return "<" + name + ">";
    }


}
