package mesut.parserx.nodes;

import mesut.parserx.gen.ll.Type;

import java.util.Objects;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public static boolean printIndex = false;
    public Node rhs;
    public Name reff;
    public int index;
    public boolean hidden = false;//if true rule has no effect
    public Type retType;//ast type in case it is modified
    public boolean isRecursive;
    public boolean isOriginal;

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this(name, null);
    }

    public RuleDecl(String name, Node rhs) {
        if (name.equals("EOF")) {
            throw new RuntimeException("rule name 'EOF' is reserved use another");
        }
        this.reff = new Name(name);
        this.rhs = rhs;
    }

    public RuleDecl(Name reff, Node rhs) {
        this.reff = reff;
        this.rhs = rhs;
    }

    public RuleDecl(Name reff) {
        this.reff = reff;
    }

    public String getName() {
        return reff.toString();
    }

    public String baseName() {
        return reff.name;
    }

    @Override
    public String toString() {
        String s;
        if (rhs.isOr()) {
            s = reff + ":\n" + rhs.asOr().withNewline() + ";";
        }
        else {
            s = reff + " = " + rhs + ";";
        }

        if (hidden) {
            return "/*" + s + "*/";
        }
        if (printIndex) {
            return "/*" + index + "*/ " + s;
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleDecl decl = (RuleDecl) o;
        return index == decl.index && Objects.equals(reff, decl.reff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reff, index);
    }

}
