package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Template;
import mesut.parserx.utils.UnicodeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Ignore
public class LexerGenTest {

    public static void generateLexer(File grammar) throws Exception {
        NFA nfa = NFA.makeNFA(grammar);
        NFA dfa = NFA.makeDFA(grammar);
        String outDir;
        outDir = Env.testJava + "/gen";
        //outDir = Main.javaDir;
        generateTest(dfa, outDir);
    }

    static void generateTest(NFA dfa, String outDir) throws IOException {
        LexerGenerator generator = new LexerGenerator(dfa, outDir);
        generator.setClassName("GeneratedLexer");
        generator.setPackageName("gen");
        generator.generate();
    }

    @Test
    public void template() throws IOException {
        Template template = new Template("token.java.template");
        template.set("package", "pkg");
        template.set("token_class", "cls");
        template.set("asd", "dummy");
        System.out.println(template);
    }

    @Test
    @Ignore
    public void all() throws Exception {
        NFA dfa = NFA.makeDFA(Env.getJavaLexer());
        File dump = new File("/home/mesut/IdeaProjects/parserx/src/test/resources/java/javaLexer.txt");
        dfa.dump(new PrintWriter(dump));
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
