package gen;
public class Token{
    public int type;
    public String value;
    public int offset;
    public String name;//token name that's declared in grammar

    public Token(){}

    public Token(int type,String value){
        this.type=type;
        this.value=value;
    }
    public String toString(){return value+" type="+type;}
}
