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
        return new File(Env.testDir, "lexer.g");
    }

    public static File getExpr() {
        return new File(Env.testDir, "expr.g");
    }

    static File getTestFile() {
        return new File(Env.javaDir, "Main.java");
    }

    static File getTestFile2() {
        return new File(Env.testDir, "test.java");
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
    public void escapeTest() {
        char[] chars = {'\n', '\r', '\t', ' ', '\0'};
        for (char c : chars) {
            System.out.println(UnicodeUtils.escapeUnicode(c));
        }
        System.out.println((int) '\u0010');
    }

}
