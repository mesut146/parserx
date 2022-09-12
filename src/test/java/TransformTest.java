import common.Env;
import mesut.parserx.gen.ll.DotBuilder;
import mesut.parserx.gen.ll.RDParserGen;
import mesut.parserx.gen.transform.*;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;
import parser.Builder;
import parser.DescTester;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TransformTest {

    void trimRest(Tree tree, String rule) {
        var usages = Optimizer.UsageCollector.collect(tree.getRule(rule).rhs, tree);
        tree.rules.removeIf(name -> !usages.contains(name.ref));
    }

    @Test
    public void recursion() throws IOException {
        Tree tree = Env.tree("rec/cyc.g");
        Recursion.debug = true;
        new Recursion(tree).all();
        trimRest(tree, "A");
        tree.printRules();
    }

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
        Tree tree = Env.tree("lr1/pred.g");
        List<String> list = new ArrayList<>();
        list.add("1+2+3+4");
        list.add("1-2*-3");
        list.add("1?2:3?4:5+6+7");//ternary
        list.add("1+2+++3++++");//post
        list.add("1.2.3+1[2][3]");//dot,array
        list.add("1.2[3]");
        list.add("(String)1+(1+2)");//cast

        DescTester.dots(tree, "E", list.toArray(new String[0]));
    }

    @Test
    public void dot() throws IOException {
        String str = "E{E2{E3{E4{PRIM{'1'}}}},Eg1{'+'},E{E2{E3{E4{PRIM{'2'}}},E2g1{'*'},E2{E3{E4{PRIM{'3'}}},E2g1{'*'},E2{E3{E4{PRIM{'4'},'^',E4{PRIM{'5'}}}}}}}}}";
        DotBuilder.write(str, new PrintWriter(new FileWriter(Env.dotFile("b"))));
    }

    @Test
    public void greedyTail() throws IOException {
        Factor.debug = true;
        Tree tree = Env.tree("greedy/a.g");
        GreedyNormalizer normalizer = new GreedyNormalizer(tree, new FactorLoop(tree, null));
        normalizer.normalize();
        tree.printRules();
        System.out.println(RegexForm.normalizeRule(tree.getRule("E"), tree));

    }

    @Test
    public void greedyRec() throws IOException {
        Factor.debug = true;
        Tree tree = Env.tree("greedy/rec.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RDParserGen.gen(tree, "java");
    }

    @Test
    public void greedy() throws Exception {
        Builder.tree("greedy/b.g").rule("E")
                .input("ca", "")
                .input("caba", "")
                .check();
        Builder.tree("greedy/a.g").rule("E")
                .input("cya", "")
                .input("cydda", "")
                .input("cyfa", "")
                .input("cyfdda", "")
                .input("cyaeba", "")
                .check();
    }
}
