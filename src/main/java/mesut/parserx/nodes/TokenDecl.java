package mesut.parserx.nodes;

public class TokenDecl {

    public String name;
    public boolean fragment = false;
    public boolean isSkip = false;
    public Node rhs;
    public Name after;

    public TokenDecl(String name) {
        this(name, null);
    }

    public TokenDecl(String name, Node rhs) {
        if (name.equals("EOF")) {
            throw new RuntimeException("token name 'EOF' is reserved use another");
        }
        this.name = name;
        this.rhs = rhs;
    }

    public Name ref() {
        return new Name(name, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fragment) {
            sb.append("#");
        }
        sb.append(name);
        if (after != null) {
            sb.append("-").append(after);
        }
        sb.append(": ");
        sb.append(rhs);
        return sb.toString();
    }


}
