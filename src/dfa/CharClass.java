package dfa;

public class CharClass
{
    public int start,end;
    public static int min=0;
    public static int max=0xffff;
    
    public static CharClass fromChar(char c){
        CharClass cc=new CharClass();
        cc.start=cc.end=c;
        return cc;
    }
    
    public static CharClass fromRange(char c){
        CharClass cc=new CharClass();
        cc.start=cc.end=c;
        return cc;
    }
    
}
