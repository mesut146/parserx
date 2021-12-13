package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.Objects;

//rule or token symbol
public class Name extends Node {

    public static boolean debug = false;
    public String name;
    public boolean isToken;
    public ArrayList<Node> args = new ArrayList<>();

    public Name(String name) {
        this.name = name;
    }

    public Name(String name, boolean isToken) {
        this.name = name;
        this.isToken = isToken;
    }

    public boolean isRule() {
        return !isToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(varString());
        sb.append(name);
        if (!args.isEmpty()) {
            sb.append("(").append(NodeList.join(args, ", ")).append(")");
        }
        if (astInfo.isFactored) {
            sb.append("()");
            //sb.append("(").append(name).append(")");
        }
        if (debug)
            sb.append(astInfo);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return isToken == name.isToken &&
                Objects.equals(this.name, name.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isToken);
    }

    public RuleDecl makeRule() {
        return new RuleDecl(this.<Name>copy());
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitName(this, arg);
    }
}
