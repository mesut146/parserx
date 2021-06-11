package mesut.parserx.nodes;

import java.util.Objects;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public static boolean printIndex = false;
    public String name;
    public Node rhs;
    public int index;

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this.name = name;
    }

    public RuleDecl(String name, Node rhs) {
        this.name = name;
        this.rhs = rhs;
    }

    public NameNode ref() {
        return new NameNode(name, false);
    }

    @Override
    public String toString() {
        if (printIndex) {
            return "/*" + index + "*/  " + name + " = " + rhs + ";";
        }
        return name + " = " + rhs + ";";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleDecl decl = (RuleDecl) o;
        return index == decl.index &&
                Objects.equals(name, decl.name) &&
                Objects.equals(rhs, decl.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rhs, index);
    }

}
