package dfa;

public class CharClass
{
    public int start,end;
    public int min=0,max=0x10ffff;
    
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
