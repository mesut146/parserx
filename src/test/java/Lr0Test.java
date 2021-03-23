import gen.lr.Lr0Generator;
import gen.lr.Lr1Generator;
import nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class Lr0Test {

    //static String gr="lr0/calc_lr.g";
    static String gr = "lr0/simple.g";

    @Ignore
    @Test
    public void lr0() throws Exception {
        File file;
        file = Env.getCalc();
        file = Env.getResFile(gr);
        //file = Env.getResFile("lr0/left.g");
        //file = Env.getResFile("lr1/calc.g");
        Tree tree = Tree.makeTree(file);
        Lr0Generator generator = new Lr0Generator(null, Env.dotDir().getAbsolutePath(), tree);
        generator.dotWriter = new PrintWriter(Env.dotFile("lr0.dot"));
        generator.generate();
    }

    @Test
    @Ignore
    public void lr1() throws Exception {
        File file;
        file = Env.getFile2("lr1/calc2.g");
        //file = Env.getFile2("lr1/calc3.g");
        //file = Env.getFile2("lr1/simple.g");
        Tree tree = Tree.makeTree(file);
        Lr1Generator generator = new Lr1Generator(null, file.getParent(), tree);
        generator.generate();
    }
}
