package parser;

import common.Env;
import mesut.parserx.gen.lr.LrCodeGen;
import mesut.parserx.gen.lr.LrType;
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

public class LrTester {


    public static void check(Builder builder, LrType type) throws Exception {
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

        new LrCodeGen(builder.tree, type).gen();

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
                    String str;
                    if (in.isFile){
                        var res = method.invoke(null, Utils.read(new File(in.input)));
                        str = res.toString();
                    }else{
                        var res = method.invoke(null, in.input);
                        str = res.toString();
                    }
                    System.out.println(str);
                    if (in.expected != null) {
                        Assert.assertEquals(in.expected, str);
                    }
                } catch (Throwable e) {
                    System.err.println("failed for input " + in.input);
                    throw e;
                }
            }
        }
    }

}
