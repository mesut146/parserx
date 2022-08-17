package parser;

import common.Env;
import mesut.parserx.gen.lr.AstBuilderGen;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.IOException;
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
        String outDir = Env.dotDir().getAbsolutePath();
        tree.options.outDir = outDir;
        CodeGen gen = new CodeGen(tree, "lr1");
        gen.gen();
        AstBuilderGen astBuilderGen = new AstBuilderGen(tree);
        astBuilderGen.gen();
        check0(tree, "AstBuilderTester", true, Arrays.asList(in));
    }

    public static void check(Tree tree, String... in) throws Exception {
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        CodeGen gen = new CodeGen(tree, "lr1");
        gen.gen();
        check0(tree, "LrTester", false, Arrays.asList(in));
    }

    public static void check0(Tree tree, String name, boolean isAst, List<String> args) throws Exception {
        String cls = name + ".java";
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

        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add("./");
        cmd.add(name);
        if (isAst) {
            cmd.add(tree.start.name);
        }
        for (String in : args) {
            List<String> list = new ArrayList<>(cmd);
            list.add(in);
            ProcessBuilder runner = new ProcessBuilder(list);
            runner.directory(out);
            runner.redirectErrorStream(true);
            Process p2 = runner.start();
            System.out.print(Utils.read(p2.getInputStream()));
            if (p2.waitFor() != 0) {
                throw new RuntimeException("err for input " + in);
            }
        }
    }

}
