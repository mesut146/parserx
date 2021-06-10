package parser;

import common.Env;
import mesut.parserx.gen.lr.LRGen;
import mesut.parserx.gen.lr.Lr0Generator;
import mesut.parserx.gen.lr.Lr1Generator;
import mesut.parserx.gen.lr.Lr1ItemSet;
import mesut.parserx.nodes.Tree;
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

    void dots(LRGen gen, File file) throws IOException {
        gen.writeTableDot();
        gen.writeGrammar(Env.dotFile(file.getName() + "2"));

        dot(gen.tableDotFile());

        File dot = Env.dotFile(file.getName() + ".dot");
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
        file = Env.getResFile("rec/cyc.g");
        //file = Env.getFile2("javaParser.g");
        Tree tree = Tree.makeTree(file);
        Lr0Generator generator = new Lr0Generator(null, Env.dotDir().getAbsolutePath(), tree);
        generator.generate();
        dots(generator, file);
    }

    @Test
    @Ignore
    public void lr1() throws Exception {
        File file;
        //file = Env.getFile2("lr1/calc2.g");
        //file = Env.getFile2("lr1/calc3.g");
        file = Env.getFile2("lr1/simple.g");
        //file = Env.getFile2("lr1/lr1.g");
        //file = Env.getResFile("rec/cyc.g");
        //file = Env.getFile2("lr1/rr.g");
        Tree tree = Tree.makeTree(file);
        Lr1ItemSet.lalr = true;
        Lr1Generator generator = new Lr1Generator(null, Env.dotDir().getAbsolutePath(), tree);
        generator.generate();
        dots(generator, file);
        //generator.merge();
    }
}
