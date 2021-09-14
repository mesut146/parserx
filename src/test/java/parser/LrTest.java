package parser;

import common.Env;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.gen.lr.LRTableGen;
import mesut.parserx.gen.lr.Lr0Generator;
import mesut.parserx.gen.lr.Lr1Generator;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LrTest {

    //static String gr="lr0/calc_lr.g";
    static String gr = "lr0/simple.g";


    void dot(File dotFile) {
        try {
            Runtime.getRuntime().exec("dot -Tpng -O " + dotFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void dots(LRTableGen<?> gen, String name) throws IOException {
        gen.writeTableDot();
        gen.writeGrammar(Env.dotFile(name + "2"));

        dot(gen.tableDotFile());

        File dot = Env.dotFile(Utils.newName(name, ".dot"));
        gen.writeDot(new PrintWriter(dot));
        dot(dot);
    }

    @Ignore
    @Test
    public void lr0() throws Exception {
        File file;
        //file = Env.getCalc();
        //file = Env.getResFile(gr);
        //file = Env.getResFile("lr0/left.g");
        //file = Env.getResFile("lr1/calc2.g");
        //file = Env.getFile2("lr1/simple.g");
        //file = Env.getResFile("rec/cyc.g");
        file = Env.getResFile("lr1/calc3.g");
        //file = Env.getFile2("javaParser.g");
        Tree tree = Tree.makeTree(file);
        Lr0Generator generator = new Lr0Generator(Env.dotDir().getAbsolutePath(), tree);
        generator.generate();
        dots(generator, file.getName());
    }

    @Test
    @Ignore
    public void lr1() throws Exception {
        File file;
        //file = Env.getFile2("lr1/calc2.g");
        file = Env.getResFile("lr1/calc3.g");
        //file = Env.getFile2("lr1/simple.g");
        //file = Env.getFile2("lr0/simple.g");
        //file = Env.getFile2("lr1/lr1.g");
        //file = Env.getResFile("rec/cyc.g");
        //file = Env.getFile2("lr1/rr.g");
        //Tree tree = Tree.makeTree(file);
        Tree tree = Env.tree("lr1/test.g");
        //Lr1ItemSet.mergeLa = true;
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Lr1Generator generator = new Lr1Generator(tree);
        generator.generate();
        generator.merge();
        dots(generator, tree.file.getName());
        CodeGen codeGen = new CodeGen(generator, false);
        codeGen.gen();
    }

    @Test
    public void real() throws Exception {
        Tree tree = Env.tree("lr1/calc3.g");
        tree.options.packageName = "lr";
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/lr";
        Lr1Generator generator = new Lr1Generator(tree);
        generator.generate();
        generator.merge();
        CodeGen codeGen = new CodeGen(generator, false);
        codeGen.gen();
    }
}
