import gen.LeftRecursive;
import nodes.NameNode;
import nodes.Node;
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
    public void split() throws Exception {
        File file = Env.getResFile("leftRec.g");
        Tree tree = Tree.makeTree(file);
        LeftRecursive leftRecursive = new LeftRecursive(tree);
        Node node = tree.getRule("A").rhs;
        LeftRecursive.SplitInfo info = leftRecursive.split(node, new NameNode("A", false));
        System.out.println("zero=" + info.zero);
        System.out.println("one=" + info.one);
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
