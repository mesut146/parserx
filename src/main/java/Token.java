public class Token{
    public int type;
    public String value;
    public int offset;

    public Token(){}

    public Token(int type,String value){
        this.type=type;
        this.value=value;
    }
    public String toString(){return value+" type="+type;}
}
