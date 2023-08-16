package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.Objects;

//rule or token symbol
public class Name extends Node implements Comparable<Name> {

    public static boolean debug = false;
    public String name;
    public boolean isToken;
    public ArrayList<Parameter> args2 = new ArrayList<>();

    public Name(String name) {
        this.name = name;
        this.isToken = false;
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
        if (!args2.isEmpty()) {
            sb.append("(").append(NodeList.join(args2, ", ")).append(")");
        }

        if (action != null) {
            sb.append(" @").append(action);
        }
        if (debug && astInfo.which.isPresent()) {
            sb.append(" #").append(astInfo.which.get());
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
                Objects.equals(this.name, name.name) && args2.equals(name.args2);
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
