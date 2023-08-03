package mesut.parserx.nodes;

public class TokenDecl {

    public String name;
    public boolean fragment = false;
    public boolean isSkip = false;
    public boolean isMore = false;
    public Node rhs;
    public String mode;
    public ModeBlock modeBlock;

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
        sb.append(": ");
        sb.append(rhs);
        if (mode != null || isSkip) {
            sb.append(" -> ");
            if (isSkip) {
                sb.append("skip");
            } else if (isMore) {
                sb.append("more");
            }
            if (mode != null) {
                if (isSkip) {
                    sb.append(", ");
                }
                sb.append(mode);
            }
        }
        sb.append(";");
        return sb.toString();
    }

    public boolean isDefault() {
        return modeBlock == null;
    }


}
