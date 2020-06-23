import dfa.DFA;
import dfa.NFA;
import gen.LexerGenerator;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LexerGenTest {

    public static File getGrammar() {
        return new File(Env.testDir, "test.g");
    }

    @Test
    public void all() throws Exception {
        NFA nfa = NfaTest.makeNFA(getGrammar());
        DFA dfa = DfaTest.makeDFA(nfa);
        String outDir;
        outDir = Env.testJava;
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }

    static void generateTest(DFA dfa, String outDir) throws FileNotFoundException {
        LexerGenerator generator = new LexerGenerator(dfa, outDir);
        generator.setClassName("GeneratedLexer");
        generator.setPackageName("gen");
        generator.generate();
    }

    static File getTestFile() {
        return new File(Env.javaDir, "Main.java");
    }

    static File getTestFile2() {
        return new File(Env.testDir, "test.java");
    }

    @Test
    public void generatedTest() throws IOException {
        Gen gen = new Gen(new FileReader(getTestFile2()));
        Token token;
        while ((token = gen.next()) != null) {
            System.out.println(token + " pos=" + token.offset);
        }
    }
}
