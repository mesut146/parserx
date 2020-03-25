package core;
import java.io.*;

public class Lexer
{
    Reader reader;
    
    public Lexer(Reader reader){
        this.reader=reader;
    }
    
    public char read() throws IOException{
        return (char)reader.read();
    }
    
    public Token nextToken() throws IOException{
        return new Token(0,"empty");
    }
}
