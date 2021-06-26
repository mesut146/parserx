package mesut.parserx.nodes;

public class TokenDecl extends Node {

    public String tokenName;
    public boolean fragment = false;
    public boolean isSkip = false;
    public Node regex;

    public TokenDecl(String tokenName) {
        this(tokenName, null);
    }

    public TokenDecl(String tokenName, Node regex) {
        if (tokenName.equals("EOF")) {
            throw new RuntimeException("token name 'EOF' is reserved use another");
        }
        this.tokenName = tokenName;
        this.regex = regex;
    }

    public Name ref() {
        return new Name(tokenName, true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fragment) {
            sb.append("#");
        }
        sb.append(tokenName);
        sb.append(" = ");
        sb.append(regex);
        return sb.toString();
    }


}
