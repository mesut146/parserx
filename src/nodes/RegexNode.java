package nodes;
import nodes.*;

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

    //convert ebnf to bnf
    public void transform() {
        if (star) {
            //r=e*;
            //e*=
        }
        else if (plus) {

        }
        else if (optional) {
            //r= e?;
            //e?= e | ;

        }
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

