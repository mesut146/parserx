package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.Objects;

//rule or token symbol
public class Name extends Node implements Comparable<Name> {

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
        var sb = new StringBuilder();
        //sb.append(varString());
        sb.append(name);
        if (astInfo.isFactored) {
            sb.append("()");
        }
        else if (!args.isEmpty()) {
            sb.append("(").append(NodeList.join(args, ", ")).append(")");
        }

        if (action != null) {
            sb.append(" @").append(action);
        }
        if (debug && astInfo.which != -1) {
            sb.append(" #").append(astInfo.which);
        }
        return sb.toString();
    }

    public String debug() {
        boolean d = debug;
        debug = true;
        String res = toString();
        debug = d;
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var name = (Name) o;
        return isToken == name.isToken &&
                Objects.equals(this.name, name.name) && args.equals(name.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isToken);
    }

    @Override
    public <R, A> R accept(Visitor<R, A> visitor, A arg) {
        return visitor.visitName(this, arg);
    }

    @Override
    public int compareTo(Name o) {
        if (isToken && !o.isToken) return -1;
        if (!isToken && o.isToken) return 1;
        return name.compareTo(o.name);
    }
}
