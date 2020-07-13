import dfa.DFA;
import dfa.NFA;
import gen.GeneratedLexer;
import gen.LexerGenerator;
import gen.Token;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexerGenTest {

    public static File getGrammar() {
        return new File(Env.testDir, "test.g");
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

    static void generateTest(DFA dfa, String outDir) throws FileNotFoundException {
        LexerGenerator generator = new LexerGenerator(dfa, outDir);
        generator.setClassName("GeneratedLexer");
        generator.setPackageName("gen");
        generator.generate();
    }

    @Test
    public void tokenizerTest() throws IOException {
        GeneratedLexer gen = new GeneratedLexer(new FileReader(getTestFile2()));
        Token token;
        while ((token = gen.next()) != null) {
            System.out.println(token + " pos=" + token.offset);
        }
    }

    @Test
    public void escapeTest() {
        char[] chars = {'\n', '\r', '\t', ' ', '\0'};
        /*for (char c : chars) {
            System.out.println(UnicodeUtils.escapeUnicode(c));
        }*/
        System.out.println((int) '\u0010');
    }

    static int[][] unpack(String str) {
        int pos = 0;
        List<int[]> list = new ArrayList<>();
        while (pos < str.length()) {
            char groupLen = str.charAt(pos++);
            int[] arr = new int[groupLen * 2];//left and right
            int arrPos = 0;
            for (int i = 0; i < groupLen; i++) {
                char c1 = str.charAt(pos++);
                char c2 = str.charAt(pos++);
                arr[arrPos++] = c1;
                arr[arrPos++] = c2;
            }
            list.add(arr);
        }
        return list.toArray(new int[0][]);
    }
}
