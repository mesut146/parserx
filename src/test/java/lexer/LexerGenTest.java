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


    @Test
    public void cppTarget() throws IOException {
        Tree tree = Env.tree("str.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath() + "/cpp";
        LexerGenerator.gen(tree, "cpp");
    }

    @Test
    public void shortCut() {
        System.out.println(Shortcut.from("line_comment"));
        System.out.println(Shortcut.from("block_comment"));
        System.out.println(Shortcut.from("ident"));
        System.out.println(Shortcut.from("integer"));
        System.out.println(Shortcut.from("decimal"));
        System.out.println(Shortcut.from("string"));
    }

    @Test
    public void real() throws Exception {
        Tree tree = Env.tree("str.g");
        RealTest.check(tree, "\"\"a");
    }

    @Test
    public void skip() throws Exception {
        Tree tree = Env.tree("lexer/skip.g");
        RealTest.check(tree, "abc0 cde  aa\nab\rmn");
    }

    @Test
    public void line() throws Exception {
        Tree tree = Env.tree("lexer/skip.g");
        RealTest.check(tree, "l1\rl2l2\nl3l3\r\nl4l4");
    }

    @Test
    public void large() throws Exception {
        Tree tree = Env.tree("java/lexer-jls.g");
        /*tree.options.lexerClass = "Lexer2";
        tree.options.tokenClass = "Token2";
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lexer/itself";
        tree.options.packageName = "lexer.itself";
        LexerGenerator generator = new LexerGenerator(tree);
        generator.generate();*/
        RealTest.check(tree, Env.getResFile("java/large.java").getAbsolutePath());
    }

    @Test
    public void dot() throws Exception {
        Tree tree = Env.tree("str.g");
        tree.makeNFA().dfa().dot(new FileWriter(Env.dotFile("a.dot")));
    }

    @Test
    public void itself2() throws Exception {
        Tree tree = Env.tree("str.g");
        tree.options.lexerClass = "Lexer2";
        tree.options.tokenClass = "Token2";
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lexer/itself";
        tree.options.packageName = "lexer.itself";
        LexerGenerator.gen(tree, "java");
    }

    @Test
    public void itself() throws Exception {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lexer/itself";
        tree.options.packageName = "lexer.itself";
        LexerGenerator.gen(tree, "java");
    }

    @Test
    public void generateLexer() throws Exception {
        Tree tree = Env.tree("str.g");
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        tree.options = options;
        LexerGenerator.gen(tree, "java");
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
        Tree tree = Env.tree("javaLexer.g");
        LexerGenerator generator = LexerGenerator.gen(tree, "java");

        NFA dfa = generator.dfa;
        dfa.dump(new FileWriter(new File(Env.dotDir(), "javaLexer.txt")));
        dfa.getAlphabet().dump(new File(Env.dotDir(), "alphabet.txt"));

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
