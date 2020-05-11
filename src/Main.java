
import dfa.DFA;
import dfa.NFA;
import grammar.GParser;
import grammar.GParserConstants;
import grammar.Token;
import grammar2.GrammarLexer;
import grammar2.GrammarParser2;
import grammar2.GrammarToken;
import nodes.Tree;

import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        String dir = "/home/mesut/IdeaProjects/parserx";
        //String dir = "/storage/emulated/0/AppProjects/parserx";
        dir += "/test/";
        String gr = dir + "test.g";
        String test = dir + "test.txt";

        cc(gr);
        //dfa();
        //cup(gr);
        //grTest(gr);
    }


    static void match(String str, DFA dfa) {
        int cur = 1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int st = dfa.getTransition(cur, c);
            if (st == 0) {
                System.out.println("no match at input " + c);
                return;
            }
            else {
                cur = st;
            }
        }
        if (dfa.isAccepting(cur)) {
            System.out.println("match success");
        }
        else {
            System.out.println("no match");
        }
    }

    static void cup(String path) throws Exception {
        //lexer lexer = new lexer(path);
        /*parser p=new parser(lexer);
        System.out.println(p.parse());*/
        /*for(int i=0;i<100;i++){
            System.out.println(lexer.next_token());
        }*/
    }


    static void cc(String path) throws Exception {
        GParser parser = new GParser(new FileReader(path));
        //tokens(parser);
        Tree tree = parser.tree();
        NFA nfa = tree.makeNFA();
        System.out.println(nfa.numStates);
        System.out.println(nfa.numInput);
        System.out.println(nfa.alphabet);
        System.out.println(nfa.inputMap);
        System.out.println(nfa.transMap);
        //nfa.dumpAlphabet();
        //System.out.println(nfa.dfa());
    }

    static void tokens(GParser parser) {
        Token t;
        while ((t = parser.getNextToken()) != null && t.kind != GParserConstants.EOF) {
            System.out.println(t.kind + " " + t.image);
        }
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
        System.out.println(parser.tree);
    }
}
