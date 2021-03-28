package mesut.parserx.nodes;

//token name ;
public class TokenDecl extends Node {

    public String tokenName;
    public boolean fragment = false;
    public boolean isSkip = false;
    public Node regex;

    public TokenDecl(String tokenName) {
        this.tokenName = tokenName;
    }

    public TokenDecl(String tokenName, Node regex) {
        this.tokenName = tokenName;
        this.regex = regex;
    }

    public void setName(String name) {
        this.tokenName = name;
    }

    public NameNode makeReference() {
        return new NameNode(tokenName, true);
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
