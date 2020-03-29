package grammar2;
import nodes.*;

public class GrammarToken extends Node
{
    public String value;
    public TokenType type;
    int pos;
    int line;

    public GrammarToken(String value, TokenType type)
    {
        this.value = value;
        this.type = type;
    }
    
    public boolean is(TokenType other){
        return type==other;
    }

    @Override
    public String toString()
    {
        return type+" : "+value+" line="+line;
    }
    
    
}
