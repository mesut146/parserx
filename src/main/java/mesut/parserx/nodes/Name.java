package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.Objects;

//rule or token symbol
public class Name extends Node {

    public static boolean debug = false;
    public static boolean autoEncode = true;
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

    public Name encode() {
        StringBuilder sb = new StringBuilder(name);
        if (!args.isEmpty()) {
            for (Node arg : args) {
                sb.append("_");
                //todo arg of arg?
                if (arg.isName()) {
                    sb.append(arg.asName().name);
                }
                else {
                    Regex regex = arg.asRegex();//todo improve
                    sb.append(regex.toString());
                }
            }
        }
        Name res = new Name(sb.toString(), false);
        res.args = new ArrayList<>(args);
        return res;
    }

    public boolean isRule() {
        return !isToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(varString());
        if (autoEncode) {
            sb.append(encode().name);
        }
        else {
            sb.append(name);
            if (!args.isEmpty()) {
                sb.append("(").append(NodeList.join(args, ", ")).append(")");
            }
        }
        if (astInfo.isFactored) {
            sb.append("(").append(name).append(")");
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
                Objects.equals(this.name, name.name) && args.equals(name.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isToken);
    }

    @Override
    public Name copy() {
        Name res = new Name(name, isToken);
        res.args = new ArrayList<>(args);
        res.astInfo = astInfo.copy();
        return res;
    }

    public RuleDecl makeRule() {
        return new RuleDecl(this);
    }
}
