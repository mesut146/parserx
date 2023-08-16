package lexer;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.Template;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.nodes.Bracket;
import mesut.parserx.nodes.Shortcut;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.UnicodeUtils;
import mesut.parserx.utils.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import parser.Builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LexerGenTest {

    @Test
    public void orTest() throws Exception {
        RealTest.check(Env.tree("lexer/or.g"), "abac");
    }

    @Test
    public void regexTest() throws Exception {
        RealTest.check(Env.tree("lexer/regex.g"), "a ab c cdd e eee");
    }

    @Test
    public void mergeBracket() {
        var br = new Bracket("[a-cd-ef-mo-pq-s]");
        var list = Bracket.merge(br.list);//a-m o-s
        System.out.println(list);
        Assert.assertEquals(list.get(0).start, 'a');
        Assert.assertEquals(list.get(0).end, 'm');
        Assert.assertEquals(list.get(1).start, 'o');
        Assert.assertEquals(list.get(1).end, 's');
    }

    @Test
    public void negate() {
        var br = new Bracket("[^ab]");
        br.normalize();
        Assert.assertEquals("[\\u0000-\\u0060, \\u0063-\\uffff]",br.ranges.toString());
    }

    @Test
    public void until() throws Exception {
        Builder.tree("lexer/until.g")
                .input("aababc","")
                .input("/*asd*a/**/","")
                .input("'''a'b''c'''","")
                .input("\"\"\"asd\"\"a\"\"\"","")
                .tokenize();
    }

    @Test
    public void cppTarget() throws IOException {
        Tree tree = Env.tree("lexer/skip.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath() + "/cpp";
        LexerGenerator.gen(tree, Lang.CPP);
        Runtime.getRuntime().exec("g++ -shared -fPIC -o l.so Lexer.cpp Token.cpp", null, new File(tree.options.outDir));
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
    public void more() throws Exception {
        Tree tree = Env.tree("lexer/more.g");
        RealTest.check(tree, "abac");
    }

    @Test
    public void line() throws Exception {
        Tree tree = Env.tree("lexer/skip.g");
        RealTest.check(tree, "l1\rl2l2\nl3l3\r\nl4l4");
    }

    @Test
    public void javaLexer() throws Exception {
        Tree tree = Env.tree("java/JavaLexer.g");
        RealTest.check(tree, true, Env.getResFile("java/a.java.res").getAbsolutePath());
    }

    @Test
    public void dot() throws Exception {
        //Tree tree = Env.tree("str.g");
        //Tree tree = Env.tree("lexer/after.g");
        Tree tree = Env.tree("lexer/regex.g");
        File dot = Utils.noext(tree,".dot");
        tree.makeNFA().dfa().dot(dot);
        Utils.runDot(dot);
    }


    @Test
    public void generateLexer() throws Exception {
        Tree tree = Env.tree("str.g");
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        tree.options = options;
        LexerGenerator.gen(tree, Lang.JAVA);
    }

    @Test
    public void template() throws IOException {
        Template template = new Template("test.template");
        template.set("name", "mesut");
        template.set("msg", "hello");
        Assert.assertEquals("abc mesut def\n" +
                "hello\n" +
                "world",template.toString());
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


    @Test
    public void action() throws Exception {
        //var tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        //dots(tree);
        RealTest.check(Env.tree("lexer/action.g"), "abac");
        //RealTest.check(tree, "@{code;asd}}{@}}@");
        //tree.options.outDir = Env.dotDir().getAbsolutePath();
        //LexerGenerator.gen(tree, Lang.JAVA);
    }

    @Test
    public void members() throws Exception {
        var tree = Env.tree("lexer/member.g");
        RealTest.check(tree, "a");
    }

    @Test
    public void act() throws Exception {
        var tree = Env.tree("lexer/action.g");
        RealTest.check(tree, "abac");
    }

    @Test
    public void unicode() throws Exception {
        RealTest.check(Env.tree("lexer/reg.g"), "[^a-Za-zA-Z0-9]", "[_\n[\\]^-]", "[\\u0000\\u0011-\\u0022]", "[ç]");
    }
}
