package parser;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.ll.RDParserGen;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.GreedyNormalizer;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LLGenTest {

    @Test
    public void cppTarget() throws IOException {
        Tree tree = Env.tree("ll/norm.g");
        tree.options.outDir = Env.dotDir() + "/cpp";
        RDParserGen.gen(tree, Lang.CPP);
    }

    @Test
    public void normalize() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Env.tree("ll/norm.g");
        tree.options = options;
        //new Normalizer(tree).normalize();
        //new Factor(tree).factorize();
        //System.out.println(tree);
        RDParserGen.gen(tree, Lang.JAVA);
    }

    @Test
    public void printer() throws Exception {
        DescTester.check(Env.tree("ll/norm.g"), "A", "ab", "cf", "ccf", "ddf", "ddcf");
    }

    @Test
    public void factorAll() throws Exception {
        Factor.debug = false;

        DescTester.check(Env.tree("factor/single.g"), "A", "ac", "eb", "adb");
        DescTester.check(Env.tree("factor/single2.g"), "A", "aac", "aadbeb");
        DescTester.check(Env.tree("factor/group.g"), "A", "ab", "ace", "de");
        DescTester.check(Env.tree("factor/list.g"), "A", "ab", "c", "ac", "aaaaaac");
        DescTester.check(Env.tree("factor/group-list.g"), "A", "c", "abc", "ababc", "e", "ade");
        DescTester.check(Env.tree("factor/eps.g"), "A", "a", "aa");
        DescTester.check(Env.tree("factor/eps.g"), "B", "ab", "a", "aa");
        DescTester.check(Env.tree("factor/double-same.g"), "A", "aab", "aac");
        DescTester.check(Env.tree("factor/double-same.g"), "B", "abc", "abd");
        DescTester.check(Env.tree("factor/double-same-extra.g"), "A", "aab", "c", "aadb", "axb", "eb");
        DescTester.check(Env.tree("factor/double-same-extra2.g"), "B", "aad", "ax", "e");
        DescTester.check(Env.tree("factor/double-same-extra2.g"), "A", "aab", "c", "aadb", "axb", "eb");

        DescTester.check(Env.tree("factor/loop.g"), "A", "aaab", "aaac");
        DescTester.check(Env.tree("factor/loop.g"), "B", "b", "aaab", "aaac");
        DescTester.check(Env.tree("factor/loop.g"), "C", "b", "c", "aaab", "aaac");
        DescTester.check(Env.tree("factor/loop.g"), "D", "ad", "aaab", "aaac");
        DescTester.check(Env.tree("factor/loop.g"), "E", "b", "dc", "ec", "af", "aaab", "aaadc", "aaaec");
//
        DescTester.check(Env.tree("factor/loop2.g"), "A", "aaac", "aaad", "aaabd", "babaad");
        DescTester.check(Env.tree("factor/loop3.g"), "E", "cdcdae", "cdcdf", "cdcdbf", "bcdcdbf");

        Factor.debug = true;
        DescTester.check(Env.tree("factor/plus_inside_plus.g"), "E", "aaac", "aaadaadb", "eeeb");
        //DescTester.check(Env.tree("factor/zero_can_be_empty.g"), "E", "aaac", "aaab","adb","aadadab");
    }

    @Test
    @Ignore
    public void parserx() throws Exception {
        Factor.debug = true;
        Tree tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        Builder.tree(tree).rule("tree").input(Utils.read(Env.getResFile("lr1/pred.g")), "");
    }

    @Test
    public void recursion() throws Exception {
        Factor.debug = true;
        GreedyNormalizer.debug = true;
        //Tree tree = Env.tree("rec/direct.g");
        //Tree tree = Env.tree("rec/direct2.g");
        //Tree tree = Env.tree("rec/indirect.g");
        //Tree tree = Env.tree("rec/expr.g");
        Tree tree = Env.tree("rec/cyc1.g");
        //new Recursion(tree).all();
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        RDParserGen.gen(tree, Lang.JAVA);
    }

    @Test
    public void recursionAll() throws Exception {
        Factor.debug = true;
//        DescTester.check(Env.tree("rec/direct.g"), "A", "c", "ca", "cb", "caa", "cab", "cba", "cbb");
//        DescTester.check(Env.tree("rec/direct2.g"), "A", "c", "cb", "cca", "ccba");
//        DescTester.check(Env.tree("rec/direct-double.g"), "A", "b", "bba", "bbbaa");
        //DescTester.check(Env.tree("rec/indirect.g"), "A", "d", "aec", "ec", "adbc", "dbc");
        //DescTester.check(Env.tree("rec/cyc1.g"), "A", "c", "db", "cab", "dbab");
        DescTester.check(Env.tree("rec/cyc1.g"), "B", "d", "ca", "dba");
    }

    @Test
    public void lr() throws IOException {
        //EbnfToBnf.leftRecursive = false;
        Tree tree = Env.tree("rec/cyc1-lr.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrDFAGen gen = new LrDFAGen(tree, LrType.LR1);
        gen.generate();
        gen.checkAndReport();
        LrTest.dots(gen, tree.file.getName());
    }
}
