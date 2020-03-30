
import grammar.GParser;
import grammar.lexer;
import grammar2.GrammarLexer;
import grammar2.GrammarParser2;
import grammar2.GrammarToken;
import nodes.Tree;

import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        String dir = "/home/mesut/IdeaProjects/parserx/";
        //String dir = "/storage/emulated/0/AppProjects/parserx/";
        String gr = dir + "test.p";
        String test = dir + "test.txt";

        cc(gr);
        //cup(gr);
        //grTest(gr);
    }

    static void cup(String path) throws Exception {
        lexer lexer = new lexer(path);
        /*parser p=new parser(lexer);
        System.out.println(p.parse());*/
        /*for(int i=0;i<100;i++){
            System.out.println(lexer.next_token());
        }*/
    }


    static void cc(String path) throws Exception {
        GParser parser = new GParser(new FileReader(path));
        Tree tree = parser.tree();

        System.out.println(tree.transform());
    }

    static void tokens(GrammarLexer lexer) throws IOException {
        for (GrammarToken token = lexer.nextToken(); token != null; token = lexer.nextToken()) {
            System.out.println(token);
        }
    }

    static void grTest(String path) throws Exception {
        GrammarLexer lexer = new GrammarLexer(new FileReader(path));
        //tokens(lexer);
        GrammarParser2 parser = new GrammarParser2(lexer);
        parser.parse();
        System.out.println(parser);
    }
}
