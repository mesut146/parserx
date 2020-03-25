package nodes;
import java.util.*;

//token name1,name2 ;
public class TokenDecl extends Node
{
    String tokenName;

    public TokenDecl(String tokenName)
    {
        this.tokenName = tokenName;
    }
    
    public void setName(String name){
        this.tokenName=name;
    }
    
    
    
    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("token: ");
        sb.append(tokenName);
        sb.append(";");
        return sb.toString();
    }
    
    
}
