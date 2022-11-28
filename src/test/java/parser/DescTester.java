package parser;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.ast.JavaAst;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.utils.Utils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DescTester {


    public static void checkTokens(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        JavaAst.printTokenQuote = false;
        ParserGen.genCC(tree, Lang.JAVA, true);

        File out = new File(outDir, "out");
        if (out.exists()) {
            Files.walkFileTree(out.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }
            });
            out.delete();
        }
        out.mkdir();

        var javac = new ProcessBuilder("javac", "-d", "./out", tester.getName());
        javac.directory(new File(outDir));
        javac.redirectErrorStream(true);
        Process p = javac.start();
        System.out.println(Utils.read(p.getInputStream()));
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
        var cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
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
                System.out.println(res);
                System.out.println(extractTokens(res));

            } catch (Throwable e) {
                System.err.println("in tree " + tree.file.getName());
                System.err.println("err for input: " + info.input);
                throw e;
            }
        }
        cl.close();
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

    static List<String> extractTokensNoQuote(String str) {
        int pos = 0;
        var list = new ArrayList<String>();
        boolean inRule = false;
        boolean inArray = false;
        while (pos < str.length()) {
            while (str.charAt(pos) == ' ') {
                pos++;
            }
            if (Character.isJavaIdentifierStart(str.charAt(pos))) {
                while (Character.isJavaIdentifierPart(str.charAt(pos)) || str.charAt(pos) == '#') {
                    pos++;
                }
                pos++;//{
                inRule = true;
            }
            else if (str.charAt(pos) == '[') {
                pos++;
                inArray = true;
            }
            else if (str.charAt(pos) == '}') {
                pos++;
                inRule = false;
            }
            else if (str.charAt(pos) == ']') {
                pos++;
                inArray = false;
            }
            else if (str.charAt(pos) == ',') {
                pos++;
            }
        }
        return list;
    }

    public static void check(Builder builder, boolean cc) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        if (cc) {
            ParserGen.genCC(tree, Lang.JAVA, true);
        }
        else {
            ParserGen.gen(tree, Lang.JAVA);
        }

        File out = new File(outDir, "out");
        if (out.exists()) {
            Files.walkFileTree(out.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }
            });
            out.delete();
        }
        out.mkdir();

        var javac = new ProcessBuilder("javac", "-d", "./out", tester.getName());
        javac.directory(new File(outDir));
        javac.redirectErrorStream(true);
        var p = javac.start();
        System.out.println(Utils.read(p.getInputStream()));

        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
        var cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
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
