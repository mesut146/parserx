package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public static boolean printIndex = false;
    public String name;
    public Node rhs;
    public int index;
    public List<Name> args = new ArrayList<>();

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this(name, null);
    }

    public RuleDecl(String name, Node rhs) {
        if (name.equals("EOF")) {
            throw new RuntimeException("rule name 'EOF' is reserved use another");
        }
        this.name = name;
        this.rhs = rhs;
    }

    public Name ref() {
        return new Name(name, false);
    }

    @Override
    public String toString() {
        if (printIndex) {
            return "/*" + index + "*/  " + name + printArgs() + " = " + rhs + ";";
        }
        return name + printArgs() + " = " + rhs + ";";
    }

    String printArgs() {
        if (args.isEmpty()) {
            return "";
        }
        return "(" + NodeList.join(args, ", ") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleDecl decl = (RuleDecl) o;
        return index == decl.index &&
                Objects.equals(name, decl.name) /*&&
                Objects.equals(rhs, decl.rhs)*/;
    }

    @Override
    public int hashCode() {
        //return Objects.hash(name, rhs, index);
        return Objects.hash(name, index);
    }

}
