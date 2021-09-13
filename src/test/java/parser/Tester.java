package parser;

import common.Env;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Tester {

    public static void check(Tree tree, String rule, String... in) throws Exception {
        File tester = new File(Env.dotDir(), "Tester.java");
        if (!tester.exists()) {
            Utils.write(Utils.read(Env.getResFile("Tester.java.1")), tester);
        }
        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        RecDescent gen = new RecDescent(tree);
        gen.gen();

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

        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./out", "Parser.java", "Tester.java");
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(read(p.getInputStream()));
            throw new RuntimeException("cant compile");
        }

        for (String s : in) {
            ProcessBuilder runner = new ProcessBuilder("java", "-cp", "./", "Tester", rule, s);
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            System.out.println(read(p2.getInputStream()));
            if (p2.waitFor() != 0) {
                throw new RuntimeException("err for input " + s);
            }
        }
    }

    static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
