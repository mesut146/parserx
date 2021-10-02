package mesut.parserx.nodes;

import mesut.parserx.gen.ll.Type;

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
    public List<Node> args = new ArrayList<>();
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
        this.name = name;
        this.rhs = rhs;
    }

    public Name ref() {
        Name res = new Name(name, false);
        res.args = new ArrayList<>(args);
        return res;
    }

    @Override
    public String toString() {
        String s;
        if (rhs.isOr()) {
            s = name + printArgs() + ":\n" + rhs.asOr().withNewline() + ";";
        }
        else {
            s = name + printArgs() + " = " + rhs + ";";
        }

        if (hidden) {
            return "/*" + s + "*/";
        }
        if (printIndex) {
            return "/*" + index + "*/ " + s;
        }
        return s;
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
        return index == decl.index && Objects.equals(name, decl.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

}
