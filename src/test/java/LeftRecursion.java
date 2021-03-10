import gen.LeftRecursive;
import nodes.Tree;
import org.junit.Test;

import java.io.File;

public class LeftRecursion {

    @Test
    public void remove() throws Exception {
        File file = Env.getResFile("leftRec.g");
        Tree tree = Tree.makeTree(file);
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        leftRecursive.process();
        System.out.println(leftRecursive.res);
    }

    @Test
    public void remove2() throws Exception {
        File file = Env.getResFile("leftRec2.g");
        Tree tree = Tree.makeTree(file);
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        leftRecursive.process();
        System.out.println(leftRecursive.res);
    }
}
