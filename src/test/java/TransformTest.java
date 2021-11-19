import common.Env;
import mesut.parserx.gen.ll.DotBuilder;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.transform.EbnfToBnf;
import mesut.parserx.gen.transform.EpsilonTrimmer;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;
import parser.DescTester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TransformTest {


    @Test
    public void ebnf() throws Exception {
        Tree tree = Env.tree("ebnf.g");
        tree = EbnfToBnf.transform(tree);
        EpsilonTrimmer.preserveNames = true;
        EpsilonTrimmer.trim(tree).printRules();
        //tree.printRules();
    }

    @Test
    @Ignore
    public void jls() throws Exception {
        Tree tree = Env.tree("java/parser-jls.g");
        //tree = EbnfToBnf.transform(tree);
        //tree = EbnfToBnf.combineOr(tree);
        tree = EpsilonTrimmer.trim(tree);
        tree.printRules();
        //Helper.revert(tree);
        //Simplify.all(tree);
    }

    @Test
    public void trim() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("eps.g");
        tree = EpsilonTrimmer.trim(tree);
        tree.printRules();
    }

    @Test
    public void precReal() throws Exception {
        Tree tree = Env.tree("pred.g");
        //DescTester.check(tree, "E", "1+2", "1+2*3", "1+2*3*4^5");
        //String out = DescTester.checkWithUrl(tree, "E", "1+2*3+4*5").toString();
        //DescTester.dots(tree, "E", "1+2+3+4", "1-2*-3","1?2:3?4:5+6+7");
        //DescTester.dots(tree, "E", "1+2+++3++++");
        DescTester.dots(tree, "E", "1.2.3");
    }

    @Test
    public void dot() throws IOException {
        String str = "E{'T1',A{'T2'}}";
        str = "E{E2{E3{E4{PRIM{'1'}}}},Eg1{'+'},E{E2{E3{E4{PRIM{'2'}}}}}}";
        str = "E{E2{E3{E4{PRIM{'1'}}}},Eg1{'+'},E{E2{E3{E4{PRIM{'2'}}},E2g1{'*'},E2{E3{E4{PRIM{'3'}}},E2g1{'*'},E2{E3{E4{PRIM{'4'},'^',E4{PRIM{'5'}}}}}}}}}";
        DotBuilder.write(str, new PrintWriter(new FileWriter(Env.dotFile("b"))));
    }
}
