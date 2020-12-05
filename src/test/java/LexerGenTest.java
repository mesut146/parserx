import dfa.DFA;
import dfa.NFA;
import gen.GeneratedLexer;
import gen.LexerGenerator;
import gen.Template;
import gen.Token;
import org.junit.Ignore;
import org.junit.Test;
import utils.Helper;
import utils.UnicodeUtils;

import java.io.*;

@Ignore
public class LexerGenTest {

    @Test
    public void template() throws IOException {
        Template template = new Template("token.java.template", "package", "token_class");
        template.set("package", "pkg");
        template.set("token_class", "cls");
        template.set("asd", "dummy");
        System.out.println(template);
    }

    public static void generateLexer(File grammar) throws Exception {
        NFA nfa = NfaTest.makeNFA(grammar);
        DFA dfa = DfaTest.makeDFA(nfa);
        String outDir;
        outDir = Env.testJava + "/gen";
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }

    static void generateTest(DFA dfa, String outDir) throws IOException {
        LexerGenerator generator = new LexerGenerator(dfa, outDir);
        generator.setClassName("GeneratedLexer");
        generator.setPackageName("gen");
        generator.generate();
    }

    @Test
    public void tokenizerTest() throws IOException {
        Reader reader = new BufferedReader(new FileReader("/home/mesut/IdeaProjects/parserx/src/test/resources/java/a.java"));
        GeneratedLexer gen = new GeneratedLexer(reader);
        Token token;
        while ((token = gen.next()) != null) {
            System.out.println(token + " pos=" + token.offset + " id=" + token.name);
        }
    }


    @Test
    @Ignore
    public void all() throws Exception {
        DFA dfa = Helper.makeDFA(Env.getJavaLexer());
        dfa.dump(new File("/home/mesut/IdeaProjects/parserx/src/test/resources/java/javaLexer.txt"));
        dfa.getAlphabet().dump(new File("/home/mesut/IdeaProjects/parserx/src/test/resources/java/javaLexer-alphabet.txt"));
        //new Analyze(dfa).analyze();
        String outDir;
        //outDir = Env.testJava + "/gen";
        //outDir = "/home/mesut/IdeaProjects/parserx/src/test/resources/java";
        outDir = "/home/mesut/IdeaProjects/parserx/src/test/java/gen";
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }


    @Test
    @Ignore
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
