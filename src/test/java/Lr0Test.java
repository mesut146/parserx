import gen.LrGenerator;
import nodes.Tree;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Lr0Test {


    @Test
    public void test() throws FileNotFoundException {
        //System.setOut(new PrintStream(Env.getFile2("stdout")));
        Tree tree = Tree.makeTree(Env.getCalc());
        LrGenerator generator = new LrGenerator(null, null, tree);
        generator.generate();
    }
}
