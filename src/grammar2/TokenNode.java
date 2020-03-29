package grammar2;
import nodes.*;

public class TokenNode extends Node
{
    public GrammarToken token;

    public TokenNode(GrammarToken token)
    {
        this.token = token;
    }

    @Override
    public String toString()
    {
        return token.toString();
    }
    
    
}
