package grammar;
import com.github.jhoenicke.javacup.runtime.*;
import grammar2.*;
import static grammar2.TokenType.*;
import java.io.*;

public class lexer implements Scanner
{
    GrammarLexer lexer;
    
    public lexer(String path) throws FileNotFoundException{
        this.lexer=new GrammarLexer(new FileReader(path));
    }

    @Override
    public Symbol next_token() throws Exception
    {
        // TODO: Implement this method
        GrammarToken t=lexer.nextToken();
        //System.out.println("t="+t);
        if(t==null){
            return new Symbol(sym.EOF);
        }
        Symbol s=null;
        
        switch(t.type){
            case IDENT:
                s=new Symbol(sym.IDENT);
                break;
            case TOKEN:
                s=new Symbol(sym.TOKEN);
                break;
            case STAR:
                s=new Symbol(sym.STAR);
                break;
            case PLUS:
                s=new Symbol(sym.PLUS);
                break;
            case LPAREN:
                s=new Symbol(sym.LPAREN);
                break;
            case RPAREN:
                s=new Symbol(sym.RPAREN);
                break;
            case QUES:
                s=new Symbol(sym.QUES);
                break;
            case EQ:
                s=new Symbol(sym.EQ);
                break;
            case OR:
                s=new Symbol(sym.OR);
                break;
            case SEMI:
                s=new Symbol(sym.SEMI);
                break;
            case COLON:
                s=new Symbol(sym.COLON);
                break;
            case COMMA:
                s=new Symbol(sym.COMMA);
                break;
        }
        s.value=t.value;
        return s;
    }

    
    
}
