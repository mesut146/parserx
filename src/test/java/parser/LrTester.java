package parser;

import common.Env;
import mesut.parserx.gen.lr.AstBuilderGen;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LrTester {

    public static void checkAst(Tree tree, String... in) throws Exception {
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        CodeGen gen = new CodeGen(tree, "lr1");
        gen.gen();
        AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();
        check0(tree, true, Arrays.asList(in));
    }

    public static void check(Tree tree, String... in) throws Exception {
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        CodeGen gen = new CodeGen(tree, "lr1");
        gen.gen();
        check0(tree, false, Arrays.asList(in));
    }

    public static void check0(Tree tree, boolean isAst, List<String> args) throws Exception {
        String cls = "LrTester.java";
        File tester = new File(Env.dotDir(), cls);
        Utils.copy(Env.getResFile(cls + ".1"), tester);
        String outDir = Env.dotDir().getAbsolutePath();

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

        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./out", cls);
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        try (var cl = new URLClassLoader(new URL[]{out.toURI().toURL()})) {
            Class<?> c = cl.loadClass("LrTester");

            if (isAst) {
                var method = c.getDeclaredMethod("testAst", String.class, String.class);
                for (String in : args) {
                    var res = method.invoke(null, tree.start.name, in);
                    System.out.println(res);
                }
            }
            else {
                var method = c.getDeclaredMethod("test", String.class);
                for (var in : args) {
                    var res = method.invoke(null, in);
                    System.out.println(res);
                }
            }
        }
    }

}
