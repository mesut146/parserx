package mesut.parserx.nodes;

import mesut.parserx.gen.lldfa.RecursionHandler;
import mesut.parserx.gen.lldfa.Type;
import mesut.parserx.gen.lr.TreeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RuleDecl {

    public static boolean printIndex = false;
    public Name ref;
    public Node rhs;
    public int index;
    public Optional<Integer> which = Optional.empty();//no alt or alt number
    public Type retType;//ast type;
    public TreeInfo.TransformInfo transformInfo;
    public RecursionHandler.Info recInfo;
    public List<Parameter> parameterList = new ArrayList<>();

    public RuleDecl(String name, Node rhs) {
        this(new Name(name), rhs);
    }

    public RuleDecl(Name ref, Node rhs) {
        if (ref.name.equals("EOF")) {
            throw new RuntimeException("rule name 'EOF' is reserved use another");
        }
        this.ref = ref;
        this.rhs = rhs;
    }

    public String getName() {
        return ref.toString();
    }

    public String baseName() {
        return ref.name;
    }

    public boolean isAlt() {
        return which.isPresent();
    }

    @Override
    public String toString() {
        String s = ref.name;
        if (!parameterList.isEmpty()) {
            s += "[" + NodeList.join(parameterList, ", ") + "]";
        }
        if (false && rhs.isOr()) {
            s += ":\n" + rhs.asOr().withNewline() + ";";
        } else {
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
        var other = (RuleDecl) o;
        return Objects.equals(ref, other.ref) && which.equals(other.which);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, which);
    }

}
