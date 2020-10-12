import gen.LrGenerator;
import nodes.Tree;
import org.junit.Test;

import java.io.File;

public class Lr0Test {


    @Test
    public void test() throws Exception {
        File file;
        //file = Env.getCalc();
        file = Env.getResFile("calc_lr.g");
        Tree tree = Tree.makeTree(file);
        LrGenerator generator = new LrGenerator(null, null, tree);
        generator.generate();
    }
}
