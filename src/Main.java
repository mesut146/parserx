
import grammar2.*;
import java.io.*;
import nodes.*;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		String gr="/storage/emulated/0/AppProjects/parserx/test.p";
        String test="/storage/emulated/0/AppProjects/parserx/test.txt";
        
        /*GParser parser=new GParser(new FileReader(gr));
        Tree tree=parser.tree();
        
        System.out.println(tree);*/
        grTest(gr);
	}
    
    
    
    static void tokens(GrammarLexer lexer) throws IOException{
        for(GrammarToken token=lexer.nextToken();token!=null;token=lexer.nextToken()){
            System.out.println(token);
        }
    }
    
    static void grTest(String path) throws Exception{
        GrammarLexer lexer=new GrammarLexer(new FileReader(path));
        //tokens(lexer);
        GrammarParser parser=new GrammarParser(lexer);
        System.out.println(parser.parse());
    }
}
