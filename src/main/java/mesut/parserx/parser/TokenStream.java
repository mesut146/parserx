package mesut.parserx.parser;
import java.util.*;
import java.io.*;

public class TokenStream{
    Lexer lexer;
    LinkedList<Token> tokens = new LinkedList<>();
    public Token la;
    int pos = 0;

    public TokenStream(Lexer lexer) throws IOException{
        this.lexer = lexer;
        this.la = lexer.next();
        tokens.add(this.la);
    }

    void pop() throws IOException{
        pos++;
        if(pos < tokens.size()){
            la = tokens.get(pos);
        }else{
            la = lexer.next();
            tokens.add(la);
        }
    }

    void pop(int type, String name) throws IOException{
        if(la.type != type){
            throw new RuntimeException("unexpected token: " + la + " expecting: " + name);
        }
        pop();
    }

    public Token consume(int type, String name) throws IOException{
        if(la.type != type){
            throw new RuntimeException("unexpected token: " + la + " expecting: " + name);
        }
        if(!tokens.isEmpty()){
            tokens.removeFirst();//cur la
        }
        Token res = la;
        //set new la
        if(tokens.isEmpty()){
            la = lexer.next();
            tokens.add(this.la);
        }else{
            la = tokens.getFirst();
        }
        return res;
    }

    public void unmark(){
        //tokens.add(la);
        this.la = tokens.getFirst();
        pos = 0;
    }
}