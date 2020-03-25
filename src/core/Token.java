package core;

//from flex lexer
public class Token
{
    public String value;
    public int type;
    
    public Token(int type,String value){
        this.type=type;
        this.value=value;
    }

    @Override
    public String toString()
    {
        return type+" "+value;
    }
    
    
}
