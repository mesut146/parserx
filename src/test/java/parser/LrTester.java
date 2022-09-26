package parser;

import common.Env;
import mesut.parserx.gen.lr.LrCodeGen;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class LrTester {

    public static void check(Tree tree, String... in) throws Exception {
        check(tree, LrType.LALR1, in);
    }

    public static void check(Tree tree, LrType type, String... in) throws Exception {
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrCodeGen gen = new LrCodeGen(tree, type);
        gen.gen();
        check0(tree, Arrays.asList(in));
    }

    public static void check0(Tree tree, List<String> args) throws Exception {
        var cls = "LrTester.java";
        var tester = new File(Env.dotDir(), cls);
        Utils.copy(Env.getResFile("LrTester.java.1"), tester);
        var outDir = Env.dotDir().getAbsolutePath();

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

        var builder = new ProcessBuilder("javac", "-d", "./out", cls);
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        var p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        try (var cl = new URLClassLoader(new URL[]{out.toURI().toURL()})) {
            Class<?> c = cl.loadClass("LrTester");
            var method = c.getDeclaredMethod("test", String.class);
            for (var in : args) {
                try {
                    var res = method.invoke(null, in);
                    System.out.println(res);
                } catch (Exception e) {
                    System.err.println("failed for input " + in);
                }
            }
        }
    }

    public static void check0(Builder builder) throws Exception {
        var cls = "LrTester.java";
        var tester = new File(Env.dotDir(), cls);
        Utils.copy(Env.getResFile("LrTester.java.1"), tester);
        var outDir = Env.dotDir().getAbsolutePath();

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

        new LrCodeGen(builder.tree, LrType.LR1).gen();

        var javac = new ProcessBuilder("javac", "-d", "./out", cls);
        javac.directory(new File(outDir));
        javac.redirectErrorStream(true);
        var p = javac.start();
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + builder.tree.file.getName());
        }

        try (var cl = new URLClassLoader(new URL[]{out.toURI().toURL()})) {
            var c = cl.loadClass("LrTester");
            var method = c.getDeclaredMethod("test", String.class);
            for (var in : builder.cases) {
                try {
                    var res = method.invoke(null, in.input);
                    System.out.println(res);
                } catch (Exception e) {
                    System.err.println("failed for input " + in.input);
                    e.printStackTrace();
                }
            }
        }
    }

}
