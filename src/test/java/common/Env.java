package common;

import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

public class Env {
    public static String dir = new File(".").getAbsolutePath();

    public static File dotDir() {
        return new File(dir, "dots");
    }

    public static File dotFile(String name) throws IOException {
        dotDir().mkdirs();
        File file = new File(dotDir(), name);
        file.createNewFile();
        return file;
    }

    public static void dot(String name, Function<File, Void> f) throws IOException {
        var dotFile = dotFile(name);
        f.apply(dotFile);
        Utils.runDot(dotFile);
    }

    public static File getResFile(String name) throws IOException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        URL url = Env.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(name + " not found");
        }
        return new File(url.getPath());
    }

    public static Tree tree(String res) throws IOException {
        var tree = Tree.makeTree(getResFile(res));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        return tree;
    }

    @Test
    public void tokenLessTest() throws IOException {
        System.out.println(Utils.fromGrammar("A: \"asd\" \"a\"*;"));
    }

    @Test
    public void tokenLessTest2() throws IOException {
        System.out.println(Utils.fromRegex("a*b"));
    }

    public static void compile(Tree tree, File source, String outDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./" + outDir, source.getAbsolutePath());
        builder.directory(source.getParentFile());
        builder.redirectErrorStream(true);
        Process p = builder.start();
        System.out.println(Utils.read(p.getInputStream()));
        if (p.waitFor() != 0) {
            System.out.println(Utils.read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }
    }

    public static void deleteInside(File dir) throws IOException {
        if (!dir.exists()) return;
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }
        });
    }
}
