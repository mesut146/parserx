package mesut.parserx.dfa.parser;

public class Token{
    public int type;
    public String value;
    public int offset;
    public int line;
    public String name;//token name that's declared in grammar

    public Token(){}

    public Token(int type){
        this.type = type;
    }

    public Token(int type, String value){
        this.type = type;
        this.value = value;
    }

    public String toString(){
        return String.format("'%s' [name = %s line = %s]",value, name, line);
    }
}
