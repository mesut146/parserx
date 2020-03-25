package grammar2;

public class GrammarToken
{
    String value;
    TokenType type;

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
        return type+" : "+value;
    }
    
    
}
