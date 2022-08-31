package mesut.parserx.nodes;

import mesut.parserx.gen.ll.Type;

import java.util.Objects;

//rule decl in grammar
//name=rules;
public class RuleDecl {

    public static boolean printIndex = false;
    public Node rhs;
    public Name ref;
    public int index;
    public int which = -1;//no alt or alt number
    public Type retType;//ast type in case it is modified

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this(name, null);
    }

    public RuleDecl(String name, Node rhs) {
        if (name.equals("EOF")) {
            throw new RuntimeException("rule name 'EOF' is reserved use another");
        }
        this.ref = new Name(name, false);
        this.rhs = rhs;
    }

    public RuleDecl(Name ref, Node rhs) {
        this.ref = ref;
        this.rhs = rhs;
    }

    public RuleDecl(Name ref) {
        this.ref = ref;
    }

    public String getName() {
        return ref.toString();
    }

    public String baseName() {
        return ref.name;
    }

    @Override
    public String toString() {
        String s = ref.name;
        if (!ref.args.isEmpty()) {
            s += "(" + NodeList.join(ref.args, ", ") + ")";
        }
        if (false && rhs.isOr()) {
            s += ":\n" + rhs.asOr().withNewline() + ";";
        }
        else {
            s += ": " + rhs + ";";
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
        RuleDecl other = (RuleDecl) o;
        return index == other.index && Objects.equals(ref, other.ref) && which == other.which;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, index);
    }

}
