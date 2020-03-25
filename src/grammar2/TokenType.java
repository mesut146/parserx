package grammar2;

public enum TokenType
{
    IDENT,
    TOKEN("token"),
    COLON(":"),
    EQ("="),
    SEMI(";"),
    DOT("."),
    COMMA(","),
    PLUS("+"),
    STAR("*"),
    OR("|"),
    QUES("?"),
    LPAREN("("),RPAREN(")"),
    LBRACKET("["),RBRACKET("]"),
    LT("<"),GT(">"),
    STRING,
    COMMENT;
    
    String value;
    
    public TokenType(String value){
        this.value=value;
    }
    public TokenType(){}
}
