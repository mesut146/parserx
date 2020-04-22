package nodes;

//token name ;
public class TokenDecl extends Node {

    String tokenName;
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
        sb.append("token ");
        sb.append(tokenName);
        sb.append(";");
        return sb.toString();
    }


}
