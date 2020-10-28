import gen.lr.Lr0Generator;
import gen.lr.Lr1Generator;
import nodes.Tree;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class Lr0Test {


    @Test
    public void lr0() throws Exception {
        File file;
        file = Env.getCalc();
        file = Env.getResFile("lr0/calc_lr.g");
        //file = Env.getResFile("lr0/left.g");
        //file = Env.getResFile("lr1/calc.g");
        Tree tree = Tree.makeTree(file);
        Lr0Generator generator = new Lr0Generator(null, null, tree);
        generator.dotWriter = new PrintWriter(Env.getFile2("lr0/lr0.dot"));
        generator.generate();
    }

    @Test
    public void lr1() throws Exception {
        File file;
        file = Env.getCalc();
        //file = Env.getResFile("lr0/calc_lr.g");
        //file = Env.getResFile("lr0/left.g");
        file = Env.getResFile("lr1/calc.g");
        Tree tree = Tree.makeTree(file);
        Lr1Generator generator = new Lr1Generator(null, null, tree);
        generator.dotWriter = new PrintWriter(Env.getFile2("lr1/lr1.dot"));
        generator.generate();
    }
}
