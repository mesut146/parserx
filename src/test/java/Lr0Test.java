import gen.Lr0Item;
import gen.Lr0Generator;
import nodes.NameNode;
import nodes.OrNode;
import nodes.Sequence;
import nodes.Tree;
import org.junit.Test;
import rule.RuleDecl;

import java.io.File;
import java.io.PrintWriter;

public class Lr0Test {


    @Test
    public void or() {
        NameNode A = new NameNode("A");
        NameNode B = new NameNode("B");
        OrNode orNode = new OrNode(Sequence.of(A, A), Sequence.of(B, new NameNode("b")));
        Lr0Item item = new Lr0Item(new RuleDecl("S'", orNode), 0);
        System.out.println(item.getDotNode());
    }

    @Test
    public void test() throws Exception {
        File file;
        file = Env.getCalc();
        //file = Env.getResFile("lr0/calc_lr.g");
        //file = Env.getResFile("lr0/left.g");
        file = Env.getResFile("lr1/calc.g");
        Tree tree = Tree.makeTree(file);
        Lr0Generator generator = new Lr0Generator(null, null, tree);
        generator.dotWriter = new PrintWriter(Env.getFile2("lr0/lr0.dot"));
        generator.generate();
    }
}
