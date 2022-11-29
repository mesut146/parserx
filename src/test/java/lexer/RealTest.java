package lexer;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RealTest {

    public static void check(Tree tree, String... in) throws Exception {
        check(tree, false, in);
    }

    public static void check(Tree tree, boolean file, String... in) throws Exception {
        File tester = new File(Env.dotDir(), "LexerTester.java");
        Utils.write(Files.readString(Env.getResFile("LexerTester.java.1").toPath()), tester);

        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        LexerGenerator.gen(tree, Lang.JAVA);

        File out = new File(outDir, "out");
        Env.deleteInside(out);
        out.mkdir();

        Env.compile(tree, tester, out.getName());

        for (String s : in) {
            ProcessBuilder runner;
            if (file) {
                runner = new ProcessBuilder("java", "-cp", "./", "LexerTester", "-file", s);
            }
            else {
                runner = new ProcessBuilder("java", "-cp", "./", "LexerTester", s);
            }
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            System.out.print(Utils.read(p2.getInputStream()));
            if (p2.waitFor() != 0) {
                throw new RuntimeException("err for input " + s);
            }
        }
    }
}
