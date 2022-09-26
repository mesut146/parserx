import common.Env;
import mesut.parserx.gen.transform.*;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Or;
import mesut.parserx.nodes.Tree;
import org.junit.Test;
import parser.Builder;

import java.io.IOException;

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
    public void factor() throws IOException {
        var tree = Env.tree("factor/single.g");
        var puller = new Puller(tree);
        var res = new Name("A").accept(puller, new Name("a", true));
        System.out.println("zero: " + res.zero + "\none: " + res.one);
        tree.printRules();
    }

    @Test
    public void trim() throws Exception {
        Or.newLine = false;
        Tree tree = Env.tree("eps.g");
        tree = EpsilonTrimmer.trim(tree);
        tree.printRules();
    }

    @Test
    public void greedyTail() throws IOException {
        var tree = Env.tree("greedy/a.g");
        var normalizer = new GreedyNormalizer(tree);
        normalizer.normalize();
        tree.printRules();
        System.out.println(RegexForm.normalizeRule(tree.getRule("E"), tree));
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
