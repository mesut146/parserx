package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.nodes.Shortcut;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Ignore
public class LexerGenTest {

    static void generateTest(NFA dfa) throws IOException {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        options.lexerClass = "GeneratedLexer";
        options.packageName = "gen";
        LexerGenerator generator = new LexerGenerator(dfa, options);
        generator.generate();
    }

    @Test
    public void shortCut() throws Exception {
        System.out.println(Shortcut.from("string"));
    }

    @Test
    public void real() throws Exception {
        Tree tree = Env.tree("str.g");
        RealTest.check(tree, "5564\"asd\\\"\"[:aa:]");
    }

    @Test
    public void dot() throws IOException {
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/resources/str.g"));
        tree.makeNFA().dfa().dot(new FileWriter("/media/mesut/SSD-DATA/IdeaProjects/parserx/dots/a.dot"));
    }

    @Test
    public void itself2() throws Exception {
        Tree tree = Env.tree("str.g");
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lexer/itself";
        tree.options.packageName = "lexer.itself";
        LexerGenerator generator = new LexerGenerator(tree);
        generator.generate();
    }

    @Test
    public void itself() throws Exception {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lexer/itself";
        tree.options.packageName = "lexer.itself";
        LexerGenerator generator = new LexerGenerator(tree);
        generator.generate();
    }

    @Test
    public void generateLexer() throws Exception {
        Tree tree = Env.tree("str.g");
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        tree.options = options;
        LexerGenerator gen = new LexerGenerator(tree);
        gen.generate();
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
        File dump = new File(Env.dotDir(), "javaLexer.txt");
        dfa.dump(new FileWriter(dump));
        dfa.getAlphabet().dump(new File(Env.dotDir(), "alphabet.txt"));
        generateTest(dfa);
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
