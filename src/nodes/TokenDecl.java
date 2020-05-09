package nodes;

//token name ;
public class TokenDecl extends Node {

    public String tokenName;
    public boolean fragment;
    public Node regex;

    public TokenDecl(String tokenName) {
        this.tokenName = tokenName;
    }

    public void setName(String name) {
        this.tokenName = name;
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
