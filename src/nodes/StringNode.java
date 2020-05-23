package nodes;

//lexer rule without regex
//can be in lexer or parser part
public class StringNode extends Node {

    public String value;
    public boolean isDot = false;//[^\n]

    public StringNode() {
    }

    public StringNode(String value) {
        this.value = value;
    }

    public Bracket toBracket() {
        if (!isDot) {
            return null;
        }
        Bracket b = new Bracket();
        b.add('\n');
        b.negate = true;
        return b;
    }

    @Override
    public String toString() {
        if (isDot) {
            return ".";
        }
        return value;
    }


}
