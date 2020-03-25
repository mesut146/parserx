package grammar2;
import java.io.*;
import java.util.*;
import static grammar2.TokenType.*;

public class GrammarLexer
{
    Reader reader;
    boolean backup=false;
    char old;
    static Map<Character,TokenType> map=new HashMap<>();
    
    
    public GrammarLexer(Reader reader){
        this.reader=reader;
        init();
    }
    
    char read() throws IOException{
        if(backup){
            backup=false;
            return old;
        }
        return old=(char)reader.read();
    }
    
    public GrammarToken nextToken() throws IOException{
        GrammarToken token=null;
        char cur=read();
        
        if(isLetter(cur)){
            StringBuilder sb=new StringBuilder();
            
            while(isLetter(cur)||isDigit(cur)){
                sb.append(cur);
                cur=read();
            }
            old=cur;
            backup=true;
            token=new GrammarToken(sb.toString(),TokenType.IDENT);
            if(isKeyword(sb.toString())){
                token.type=TokenType.TOKEN;
            }
        }
        else if(map.containsKey(cur)){
            token=new GrammarToken(""+cur,map.get(cur));
        }else if(cur=='"'){
            StringBuilder sb=new StringBuilder();
            
            cur=read();
            while(cur!='"'){
                sb.append(cur);
                cur=read();
            }
            token=new GrammarToken(sb.toString(),TokenType.STRING);
        }else if(isWs(cur)){
            token=nextToken();
        }else if(cur=='/'){
            char next=read();
            if(next=='/'){
                eat('\n');
                return nextToken();
            }
        }
        return token;
    }
    
    void eat(char end) throws IOException{
        while(read()!=end){
            
        }
        backup=true;
    }
    
    boolean isKeyword(String str){
        return str.equals("token");
    }
    
    boolean isLetter(char chr){
        return (chr>='a'&&chr<='z')||(chr>='A'&&chr<='Z');
    }
    
    boolean isDigit(char chr){
        return chr>='0'&&chr<='9';
    }
    
    boolean isWs(char chr){
        return " \n\r\t".indexOf(chr)!=-1;
    }
    
    void add(TokenType... arr){
        for(TokenType tt:arr){
            map.put(tt.value.charAt(0),tt);
        }
    }
    
    void init(){
        add(SEMI,DOT,COMMA,COLON,STAR,PLUS,QUES,OR,EQ);
        add(LPAREN,RPAREN);
        add(LBRACKET,RBRACKET);
    }
}
