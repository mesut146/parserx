package parser;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.ast.JavaAst;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Assert;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DescTester {


    public static void checkTokens(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        JavaAst.printTokenQuote = true;
        ParserGen.genCC(tree, Lang.JAVA);

        File classDir = new File(outDir, "out");
        Env.deleteInside(classDir);
        classDir.mkdirs();

        Env.compile(tree, tester, classDir.getName());
        var cl = new URLClassLoader(new URL[]{classDir.toURI().toURL()});
        var cls = cl.loadClass("DescTester");
        var method = cls.getDeclaredMethod("test", String.class, String.class);
        for (var info : builder.cases) {
            try {
                String res;
                if (info.isFile) {
                    System.out.println("testing input " + info.input);
                    res = method.invoke(null, info.rule, Utils.read(new File(info.input))).toString();
                }
                else {
                    res = method.invoke(null, info.rule, info.input).toString();
                }
                System.out.println(res);
                var real = extractTokens(res);
                var lexerTokens = getLexerTokens(tree, classDir, info);
                Assert.assertEquals(real.size(), lexerTokens.size());
                System.out.println("token count: " + real.size());
                for (int i = 0; i < real.size(); i++) {
                    Assert.assertEquals(real.get(i), lexerTokens.get(i));
                }

            } catch (Throwable e) {
                System.err.println("in tree " + tree.file.getName());
                System.err.println("err for input: " + info.input);
                throw e;
            }
        }
        cl.close();
    }

    @SuppressWarnings("unchecked")
    static List<String> getLexerTokens(Tree tree, File classDir, Builder.RuleInfo info) throws Exception {
        File tester = new File(Env.dotDir(), "LexerTester.java");
        Utils.write(Files.readString(Env.getResFile("LexerTester.java.1").toPath()), tester);
        Env.compile(tree, tester, classDir.getName());
        try (var cl = new URLClassLoader(new URL[]{classDir.toURI().toURL()})) {
            var cls = cl.loadClass("LexerTester");
            var method = cls.getDeclaredMethod("tokens", String.class);
            if (info.isFile) {
                String data = Utils.read(new File(info.input));
                return (ArrayList<String>) method.invoke(null, data);
            }
            else {
                return (ArrayList<String>) method.invoke(null, info.input);
            }
        }
    }

    static List<String> extractTokens(String out) {
        int pos = 0;
        var list = new ArrayList<String>();
        while (true) {
            int quote = out.indexOf("'", pos);
            if (quote == -1) break;
            if (out.charAt(quote - 1) == '\\') break;
            var end = out.indexOf("'", quote + 1);
            while (out.charAt(end - 1) == '\\') {
                end = out.indexOf("'", end + 1);
            }
            list.add(out.substring(quote + 1, end));
            pos = end + 1;
        }
        return list;
    }

    public static void check(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        ParserGen.genCC(tree, Lang.JAVA);
        File classDir = new File(outDir, "out");
        classDir.mkdirs();
        Env.deleteInside(classDir);

        Env.compile(tree, tester, classDir.getName());

        var cl = new URLClassLoader(new URL[]{classDir.toURI().toURL()});
        var cls = cl.loadClass("DescTester");
        var method = cls.getDeclaredMethod("test", String.class, String.class);
        for (var info : builder.cases) {
            try {
                String res;
                if (info.isFile) {
                    res = method.invoke(null, info.rule, Utils.read(new File(info.input))).toString();
                }
                else {
                    res = method.invoke(null, info.rule, info.input).toString();
                }
                if (info.expected == null) {
                    System.out.println(res);
                }
                else {
                    Assert.assertEquals(info.expected, res);
                }
            } catch (Throwable e) {
                System.err.println("in tree " + tree.file.getName());
                System.err.println("err for input: " + info.input);
                throw e;
            }
        }
        cl.close();
    }

}
