package parser;

import common.Env;
import lexer.RealTest;
import mesut.parserx.gen.ll.DotBuilder;
import mesut.parserx.gen.ll.RDParserGen;
import mesut.parserx.gen.lldfa.CcGenJava;
import mesut.parserx.gen.lldfa.JavaGen;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DescTester {
    public static boolean dump = true;


    public static void checkTokens(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        if (dump) {
            tree.options.dump = true;
        }
        //RecDescent.gen(tree, "java");
        //new CcGenJava(tree).gen();
        ParserGen.gen(tree, "java");

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
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
        var cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
        var cls = cl.loadClass("DescTester");
        var method = cls.getDeclaredMethod("test", String.class, String.class);
        for (var info : builder.cases) {
            try {
                String res = method.invoke(null, info.rule, Utils.read(new File(info.input))).toString();
                //RealTest.check(tree, true, info.input);
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
            System.out.println(list.get(list.size() - 1));
            pos = end + 1;
        }
        return list;
    }

    public static void check(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.1"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        //RecDescent.gen(tree, "java");
        if (dump) {
            tree.options.dump = true;
        }
        ParserGen.gen(tree, "java");

        var out = new File(outDir, "out");
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
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
        for (Builder.RuleInfo info : builder.cases) {
            var runner = new ProcessBuilder("java", "-cp", "./", "DescTester", info.rule, info.input);
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            try {
                Assert.assertEquals(info.expected, Utils.read(p2.getInputStream()));
            } catch (Throwable e) {
                System.err.println("in tree " + tree.file.getName());
                System.err.println("err for input: " + info.input);
                throw e;
            }
            if (p2.waitFor() != 0) {
                System.err.println("in tree " + tree.file.getName());
                throw new RuntimeException("err for input " + info.input);
            }
        }
    }

    public static void check2(Builder builder) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        var tree = builder.tree;
        tree.options.outDir = outDir;
        if (dump) {
            tree.options.dump = true;
        }
        //RecDescent.gen(tree, "java");
        //new CcGenJava(tree).gen();
        ParserGen.gen(tree, "java");

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
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
        var cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
        var cls = cl.loadClass("DescTester");
        var method = cls.getDeclaredMethod("test", String.class, String.class);
        for (var info : builder.cases) {
            try {
                String res = method.invoke(null, info.rule, info.input).toString();
                Assert.assertEquals(info.expected, res);
            } catch (Throwable e) {
                System.err.println("in tree " + tree.file.getName());
                System.err.println("err for input: " + info.input);
                throw e;
            }
        }
        cl.close();
    }


    public static void check(Tree tree, String rule, String... in) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        Utils.copy(Env.getResFile("DescTester.java.1"), tester);
        var outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        //RecDescent.gen(tree, "java");
        ParserGen.gen(tree, "java");

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
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        for (var s : in) {
            var runner = new ProcessBuilder("java", "-cp", "./", "DescTester", rule, s);
            runner.directory(out);
            runner.redirectErrorStream(true);
            var runnerProc = runner.start();
            System.out.println(Utils.read(runnerProc.getInputStream()));
            if (runnerProc.waitFor() != 0) {
                throw new RuntimeException("err for input " + s);
            }
        }
    }

    public static List<Object> checkWithUrl(Tree tree, String rule, String... in) throws Exception {
        var tester = new File(Env.dotDir(), "DescTester.java");
        var outDir = Env.dotDir().getAbsolutePath();
        Utils.copy(Env.getResFile("DescTester.java.2"), tester);
        tree.options.outDir = outDir;
        RDParserGen.gen(tree, "java");

        var out = new File(outDir, "out");
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
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        var cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
        var cls = cl.loadClass("DescTester");
        var method = cls.getDeclaredMethod("test", String.class, String.class);
        var res = new ArrayList<Object>();
        for (var s : in) {
            res.add(method.invoke(null, rule, s));
        }
        cl.close();
        return res;
    }

    public static void dots(Tree tree, String rule, String... args) throws Exception {
        var list = checkWithUrl(tree, rule, args);
        int i = 0;
        for (Object out : list) {
            var dot = Env.dotFile(tree.file.getName() + i + ".dot");
            DotBuilder.write(out.toString(), new PrintWriter(new FileWriter(dot)));
            Env.dot(dot);
            i++;
        }
    }

}
