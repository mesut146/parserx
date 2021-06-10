package mesut.parserx.nodes;

import java.util.Objects;

//todo merge into this
public class Decl {
    public boolean fragment = false;
    public boolean isSkip = false;
    public String name;
    public Node rhs;
    public int index;
    public boolean isToken;

    public static Decl makeToken(String name, Node rhs) {
        Decl decl = new Decl();
        decl.name = name;
        decl.rhs = rhs;
        decl.isToken = true;
        return decl;
    }

    public NameNode ref() {
        return new NameNode(name, isToken);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fragment) {
            sb.append("#");
        }
        sb.append(name);
        sb.append(" = ");
        sb.append(rhs);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Decl decl = (Decl) o;
        return index == decl.index && name.equals(decl.name) && rhs.equals(decl.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rhs, index);
    }
}
