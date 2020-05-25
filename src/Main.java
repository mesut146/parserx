
import dfa.CharClass;
import dfa.DFA;
import dfa.NFA;
import grammar.GParser;
import grammar.GParserConstants;
import grammar.Token;
import grammar2.GrammarLexer;
import grammar2.GrammarParser2;
import grammar2.GrammarToken;
import nodes.Bracket;
import nodes.RangeNode;
import nodes.Tree;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        String dir;
        dir = "/home/mesut/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        dir += "/test/";
        String gr = dir + "test.g";
        String test = dir + "test.txt";

        //cc(gr);
        nfaToDfaTest();
        //bracketTest();
        //segmentTest();
        /*System.out.println(Integer.toHexString((int)Character.MAX_VALUE));
        System.out.println(Integer.toHexString((int)Character.MAX_CODE_POINT));
        System.out.println((char)Character.MAX_CODE_POINT);*/
        //dfa();
        //cup(gr);
        //grTest(gr);
    }

    static void nfaToDfaTest() throws IOException {
        NFA nfa = new NFA(100);
        nfa.initial = 1;
        nfa.addTransitionRange(1, 2, 0, 0);
        nfa.addEpsilon(1, 3);
        nfa.addEpsilon(3, 2);
        nfa.addTransitionRange(3, 4, 0, 0);
        nfa.addTransitionRange(4, 3, 0, 0);
        nfa.addTransitionRange(2, 2, 1, 1);
        nfa.addTransitionRange(2, 4, 1, 1);
        nfa.setAccepting(3, true);
        nfa.setAccepting(4, true);
        nfa.numStates = 4;
        nfa.dump("");
        DFA dfa = nfa.dfa();
        dfa.dump("");
    }

    static void bracketTest() {
        Bracket b = new Bracket();
        b.add(new RangeNode(10, 15));
        b.add(new RangeNode(20, 30));
        b.add(new RangeNode(25, 28));
        //b.add(new RangeNode(25, 50));
        b.add(new RangeNode(35, 50));
        System.out.println("negated=" + b.negateAll());
    }

    static void segmentTest() {
        int l = 'a';
        int r = 'z';
        int seg = CharClass.segment(l, r);
        System.out.println(Arrays.toString(CharClass.desegment(seg)));
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
        //System.out.println(tree);
        NFA nfa = tree.makeNFA();
        System.out.println(nfa.numStates);
        System.out.println(nfa.numInput);
        System.out.println(nfa.alphabet);
        //System.out.println(nfa.inputMap);
        //System.out.println(nfa.transMap);
        nfa.dump("");
        System.out.println(Arrays.toString(nfa.names));
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
