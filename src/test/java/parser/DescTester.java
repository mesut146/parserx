package parser;

import common.Env;
import mesut.parserx.gen.ll.DotBuilder;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.lldfa.LLDFAGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

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

public class DescTester {

    public static void check(Tree tree, String rule, String... in) throws Exception {
        File tester = new File(Env.dotDir(), "DescTester.java");
        Utils.write(Utils.read(Env.getResFile("DescTester.java.1")), tester);

        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        //RecDescent.gen(tree, "java");
        LLDFAGen.gen(tree, "java");

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

        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./out", tester.getName());
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        for (String s : in) {
            ProcessBuilder runner = new ProcessBuilder("java", "-cp", "./", "DescTester", rule, s);
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            System.out.println(Utils.read(p2.getInputStream()));
            if (p2.waitFor() != 0) {
                throw new RuntimeException("err for input " + s);
            }
        }
    }

    public static List<Object> checkWithUrl(Tree tree, String rule, String... in) throws Exception {
        File tester = new File(Env.dotDir(), "DescTester.java");
        Utils.write(Utils.read(Env.getResFile("DescTester.java.2")), tester);

        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        RecDescent.gen(tree, "java");

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

        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./out", tester.getName());
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        URLClassLoader cl = new URLClassLoader(new URL[]{out.toURI().toURL()});
        Class<?> cls = cl.loadClass("DescTester");
        Method method = cls.getDeclaredMethod("test", String.class, String.class);
        List<Object> res = new ArrayList<>();
        for (String s : in) {
            res.add(method.invoke(null, rule, s));
        }
        return res;
    }

    public static void dots(Tree tree, String rule, String... args) throws Exception {
        List<Object> list = checkWithUrl(tree, rule, args);
        int i = 0;
        for (Object out : list) {
            File dot = Env.dotFile(tree.file.getName() + i + ".dot");
            DotBuilder.write(out.toString(), new PrintWriter(new FileWriter(dot)));
            Env.dot(dot);
            i++;
        }
    }

}
