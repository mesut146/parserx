package grammar2;
import java.io.*;
import java.util.*;
import static grammar2.TokenType.*;

public class GrammarLexer
{
    Reader reader;
    boolean backup=false;
    char old;
    int pos=0;
    int line=1;
    static Map<Character,TokenType> map=new HashMap<>();


    public GrammarLexer(Reader reader){
        this.reader = reader;
        init();
    }

    char read() throws IOException{
        if (backup){
            backup = false;
            return old;
        }
        pos++;
        old = (char)reader.read();
        if (old == '\n'){
            line++;
        }
        return old;
    }

    public GrammarToken nextToken() throws IOException{
        GrammarToken token=null;
        int pos=this.pos;
        int line=this.line;
        char cur=read();

        if (isLetter(cur)){
            StringBuilder sb=new StringBuilder();

            while (isLetter(cur) || isDigit(cur)){
                sb.append(cur);
                cur = read();
            }
            old = cur;
            backup = true;
            token = new GrammarToken(sb.toString(), TokenType.IDENT);
            
            if (token.value.equals("token")){
                token.type = TokenType.TOKEN;
            }
            else if (token.value.equals("tokens")){
                token.type = TokenType.TOKENS;
            }
            else if (token.value.equals("skip")){
                token.type = TokenType.SKIP;
            }
        }
        else if (map.containsKey(cur)){//symbol
            token = new GrammarToken("" + cur, map.get(cur));
        }else if (cur == '"'){
            StringBuilder sb=new StringBuilder();
            cur = read();
            while (cur != '"'){
                sb.append(cur);
                cur = read();
            }
            token = new GrammarToken(sb.toString(), TokenType.STRING);
        }else if (isWs(cur)){
            return nextToken();
        }else if (cur == '/'){
            char next=read();
            if (next == '/'){
                eat('\n');
                return nextToken();
            }
            else if(next=='*'){
                eat('*','/');
                return nextToken();
            }
        }
        else if(cur=='\\'){
            char next=read();
            token=new GrammarToken("\\"+next,ESCAPED);
        }
        token.pos = pos;
        token.line = line;
        return token;
    }

    void eat(char end) throws IOException{
        while (read() != end){

        }
        backup = true;
    }
    
    void eat(char c1,char c2) throws IOException{
        
        while (read() != c1){

        }
        if(read()!=c2){
            eat(c1,c2);
        }
    }

    boolean isKeyword(String str){
        return str.equals("token");
    }

    boolean isLetter(char chr){
        return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z');
    }

    boolean isDigit(char chr){
        return chr >= '0' && chr <= '9';
    }

    boolean isWs(char chr){
        return " \n\r\t".indexOf(chr) != -1;
    }

    void add(TokenType... arr){
        for (TokenType tt:arr){
            map.put(tt.value.charAt(0), tt);
        }
    }

    void init(){
        add(SEMI, DOT, COMMA, COLON, STAR, PLUS, QUES, OR, EQ);
        add(TILDE, XOR);
        add(LPAREN, RPAREN);
        add(LBRACE,RBRACE);
        add(LBRACKET, RBRACKET);
    }
}
