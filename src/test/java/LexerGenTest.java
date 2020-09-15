import dfa.DFA;
import dfa.NFA;
import gen.GeneratedLexer;
import gen.LexerGenerator;
import gen.Token;
import org.junit.Test;
import utils.UnicodeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public class LexerGenTest {

    public static File getGrammar() {
        return Env.getFile("javaLexer.g");
    }

    public static File getCalc() {
        return Env.getFile("calc.g");
    }

    public static void generateLexer(File grammar) throws Exception {
        NFA nfa = NfaTest.makeNFA(grammar);
        DFA dfa = DfaTest.makeDFA(nfa);
        String outDir;
        outDir = Env.testJava + "/gen";
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }

    static void generateTest(DFA dfa, String outDir) throws FileNotFoundException {
        LexerGenerator generator = new LexerGenerator(dfa, outDir);
        generator.setClassName("GeneratedLexer");
        generator.setPackageName("gen");
        generator.generate();
    }

    public static void tokenizerTest(Reader reader) throws IOException {
        GeneratedLexer gen = new GeneratedLexer(reader);
        Token token;
        while ((token = gen.next()) != null) {
            System.out.println(token + " pos=" + token.offset + " id=" + token.name);
        }
    }

    @Test
    public void all() throws Exception {
        NFA nfa = NfaTest.makeNFA(getGrammar());
        DFA dfa = DfaTest.makeDFA(nfa);
        String outDir;
        outDir = Env.testJava + "/gen";
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }

    @Test
    public void escapeTest() {
        char[] chars = {'\n', '\r', '\t', ' ', '\0'};
        String[] strArr = {"\\n", "\\r", "\\t", "\\u0020", "\\u0000"};

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            String escaped = UnicodeUtils.escapeUnicode(ch);
            assert escaped.equals(strArr[i]);
            System.out.println(escaped);
        }
    }

}
