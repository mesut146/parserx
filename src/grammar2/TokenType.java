package grammar2;

public enum TokenType
{
    IDENT,
    ESCAPED,
    TOKEN("token"),
    TOKENS("tokens"),
    SKIP("skip"),
    COLON(":"),
    EQ("="),
    SEMI(";"),
    DOT("."),
    COMMA(","),
    PLUS("+"),
    STAR("*"),
    OR("|"),
    QUES("?"),
    TILDE("~"),//until
    XOR("^"),//not
    MINUS("-"),//range
    LPAREN("("),RPAREN(")"),
    LBRACE("{"),RBRACE("}"),
    LBRACKET("["),RBRACKET("]"),
    LT("<"),GT(">"),
    STRING,
    COMMENT;
    
    String value;
    
    TokenType(String value){
        this.value=value;
    }
    TokenType(){}
}
