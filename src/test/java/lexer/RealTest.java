package lexer;

import common.Env;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RealTest {

    public static void check(Tree tree, String... in) throws Exception {
        File tester = new File(Env.dotDir(), "LexerTester.java");
        Utils.write(Utils.read(Env.getResFile("LexerTester.java.1")), tester);

        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        LexerGenerator.gen(tree, "java");

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

        ProcessBuilder builder = new ProcessBuilder("javac", "-d", "./out", "LexerTester.java");
        builder.directory(new File(outDir));
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (p.waitFor() != 0) {
            System.out.println(read(p.getInputStream()));
            throw new RuntimeException("cant compile " + tree.file.getName());
        }

        for (String s : in) {
            ProcessBuilder runner;
            if (Files.exists(Paths.get(s))) {
                runner = new ProcessBuilder("java", "-cp", "./", "LexerTester", "-file", s);
            }
            else {
                runner = new ProcessBuilder("java", "-cp", "./", "LexerTester", s);
            }
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            System.out.print(read(p2.getInputStream()));
            if (p2.waitFor() != 0) {
                throw new RuntimeException("err for input " + s);
            }
        }
    }

    static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
