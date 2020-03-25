package core;
import java.io.*;
import nodes.*;

public class GeneratedLexer extends Lexer
{
    Tree tree;
    
    public GeneratedLexer(Reader reader){
        super(reader);
    }
    
    public void init(Tree tree){
        this.tree=tree;
    }

    @Override
    public Token nextToken() throws IOException
    {
        char chr=read();
        System.out.println(chr);
        for(Node node:tree.list){
            /*if(node.canMatch(chr)){
                return new Token(node.getType(),""+chr);
            }*/
        }
        return super.nextToken();
    }
    
    
}
