
import grammar2.GrammarLexer;
import grammar2.GrammarParser;
import grammar2.GrammarToken;

import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        String dir = "/home/mesut/IdeaProjects/parserx/";
        String gr = dir + "test.p";
        String test = dir + "test.txt";
        
        /*GParser parser=new GParser(new FileReader(gr));
        Tree tree=parser.tree();
        
        System.out.println(tree);*/
        grTest(gr);
    }


    static void tokens(GrammarLexer lexer) throws IOException {
        for (GrammarToken token = lexer.nextToken(); token != null; token = lexer.nextToken()) {
            System.out.println(token);
        }
    }

    static void grTest(String path) throws Exception {
        GrammarLexer lexer = new GrammarLexer(new FileReader(path));
        //tokens(lexer);
        GrammarParser parser = new GrammarParser(lexer);
        System.out.println(parser.parse());
    }
}
