package nodes;

import java.util.Objects;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public String name;
    public Node rhs;//sequence,or,simple rule
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

    @Override
    public String toString() {
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
