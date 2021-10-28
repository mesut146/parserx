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
        if (rhs.isOr()) {
            s += ":\n" + rhs.asOr().withNewline() + ";";
        }
        else {
            s += ": " + rhs + ";";
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
        return index == decl.index && Objects.equals(ref, decl.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, index);
    }

}
