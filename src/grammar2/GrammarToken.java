package grammar2;

public class GrammarToken
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
