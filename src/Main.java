
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    static String dir;

    public static void main(String[] args) throws Exception {
        dir = "/home/mesut/IdeaProjects/parserx";
        //dir = "/storage/emulated/0/AppProjects/parserx";
        dir += "/test/";

        cc("test.g");
        //nfaToDfaTest();
        //nfaToDfaTest2();
        //nfaToDfaTest3();
        //nfaToDfaTest4();
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
        int zero = '0';
        int one = '1';
        nfa.addTransitionRange(1, 2, zero, zero);
        nfa.addEpsilon(1, 3);
        nfa.addEpsilon(3, 2);
        nfa.addTransitionRange(3, 4, zero, zero);
        nfa.addTransitionRange(4, 3, zero, zero);
        nfa.addTransitionRange(2, 2, one, one);
        nfa.addTransitionRange(2, 4, one, one);
        nfa.setAccepting(3, true);
        nfa.setAccepting(4, true);
        nfa.numStates = 4;
        nfa.dump("");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
    }

    static void nfaToDfaTest2() throws IOException {
        NFA nfa = new NFA(100);
        nfa.initial = 1;
        int zero = '0';
        int one = '1';
        nfa.addTransition(1, 2, 0);
        nfa.addEpsilon(1, 3);
        nfa.addEpsilon(3, 2);
        nfa.addTransition(3, 4, 0);
        nfa.addTransition(4, 3, 0);
        nfa.addTransition(2, 2, 1);
        nfa.addTransition(2, 4, 1);
        nfa.setAccepting(3, true);
        nfa.setAccepting(4, true);
        nfa.numStates = 4;
        nfa.dump("");
        nfa.dot("/home/mesut/IdeaProjects/parserx/test/asd.dot");
        System.out.println("-------------");
        /*DFA dfa = nfa.dfa();
        dfa.dump("");*/
    }

    //multiple
    static void nfaToDfaTest3() throws IOException {
        NFA nfa = new NFA(100);
        //nfa.initial = 0;
        nfa.addTransitionRange(0, 1, '0', '0');
        nfa.addTransitionRange(1, 2, 'x', 'x');
        nfa.addTransitionRange(0, 3, '0', '0');
        nfa.addTransitionRange(3, 4, 'x', 'x');
        nfa.addTransitionRange(3, 6, 't', 't');
        nfa.addTransitionRange(4, 5, 'y', 'y');
        nfa.addTransitionRange(0, 7, '1', '1');
        nfa.addTransitionRange(7, 1, '2', '2');
        nfa.addEpsilon(4, 1);
        nfa.addEpsilon(5, 1);
        nfa.setAccepting(2, true);
        nfa.setAccepting(5, true);
        nfa.setAccepting(6, true);
        nfa.numStates = 7;
        nfa.dump("");
        //nfa.dot(dir + "asd.dot");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
        dfa.dot(dir + "asd.dot");
    }

    //javapoint
    static void nfaToDfaTest4() throws IOException {
        NFA nfa = new NFA(100);
        //nfa.initial = 0;
        nfa.addTransitionRange(0, 0, '0', '0');
        nfa.addEpsilon(0, 1);
        nfa.addTransitionRange(1, 1, '1', '1');
        nfa.addEpsilon(1, 2);
        nfa.addTransitionRange(2, 2, '2', '2');
        nfa.setAccepting(2, true);
        nfa.numStates = 2;
        //nfa.dump("");
        //nfa.dot("/home/mesut/IdeaProjects/parserx/test/asd.dot");
        System.out.println("-------------");
        DFA dfa = nfa.dfa();
        dfa.dump("");
        dfa.dot(dir + "asd.dot");
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

    static void cc(String name) throws Exception {
        File file = new File(dir, name);
        GParser parser = new GParser(new FileReader(file));
        //tokens(parser);
        Tree tree = parser.tree();
        //System.out.println(tree);
        //System.out.println("----------");
        tree.makeDistincRanges();
        //System.out.println(tree);
        NFA nfa = tree.makeNFA();
        System.out.println("total nfa states=" + nfa.numStates);
        //nfa.dump("");
        //nfa.dot(dir + "asd.dot");
        System.out.println("-----DFA-----");
        DFA dfa = nfa.dfa();
        dfa.optimize();
        System.out.println("total dfa states=" + dfa.numStates);
        //dfa.dump("");
        dfa.dot(dir + "asd.dot");
        test.testDFA(dfa);
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

    static void cup(String path) throws Exception {
        //lexer lexer = new lexer(path);
        /*parser p=new parser(lexer);
        System.out.println(p.parse());*/
        /*for(int i=0;i<100;i++){
            System.out.println(lexer.next_token());
        }*/
    }


    static void grTest(String path) throws Exception {
        GrammarLexer lexer = new GrammarLexer(new FileReader(path));
        //tokens(lexer);
        GrammarParser2 parser = new GrammarParser2(lexer);
        parser.parse();
        System.out.println(parser.tree);
    }
}
