package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.Objects;

//rule or token symbol
public class Name extends Node {

    public static boolean tokenBrace = false;
    public String name;
    public boolean isToken;//if we reference to a token
    public ArrayList<Name> args = new ArrayList<>();

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
        sb.append(varString());
        if (isToken && tokenBrace) {
            sb.append("{").append(name).append("}");
        }
        else {
            sb.append(name);
            if (!args.isEmpty()) {
                sb.append("(").append(NodeList.join(args, ", ")).append(")");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return isToken == name.isToken &&
                Objects.equals(this.name, name.name) && args.equals(name.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isToken);
    }

    @Override
    public Node copy() {
        return new Name(name, isToken);
    }
}
