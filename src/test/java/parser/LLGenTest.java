package parser;

import common.Env;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.VisitorGenerator;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.transform.Epsilons;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LLGenTest {

    @Test
    public void normalize() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Env.tree("ll/norm.g");
        tree.options = options;
        //new Normalizer(tree).normalize();
        //new Factor(tree).factorize();
        //System.out.println(tree);
        new RecDescent(tree).gen();
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
    }

    @Test
    public void eps() throws Exception {
        Tree tree = Env.tree("factor/eps.g");
        System.out.println(Epsilons.trim(tree.getRule("C").rhs, tree));
    }

    @Test
    public void factored() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("factor/double-same.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("factor/double-same-extra.g"));
        Tree tree = Env.tree("factor/double-same-extra2.g");
        //Tree tree = Tree.makeTree(Env.getResFile("factor/eps.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("factor/list.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("factor/group-list.g"));

        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        tree.options = options;
        RecDescent gen = new RecDescent(tree);
        Name.debug = false;
        Factor.debug = true;
        gen.gen();
    }

    @Test
    public void all() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Env.tree("calc-1.g");
        tree.options = options;
        new LexerGenerator(tree).generate();
        RecDescent gen = new RecDescent(tree);
        gen.gen();
    }

    @Test
    public void visitor() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        options.parserClass = "Test";
        options.genVisitor = true;
        Tree tree = Env.tree("parserx.g");
        tree.options = options;
        VisitorGenerator visitorGenerator = new VisitorGenerator(tree);
        visitorGenerator.generate();
    }

    @Test
    @Ignore
    public void parserx() throws Exception {
        Factor.factorSequence = true;
        Factor.debug = true;
        Tree tree = Env.tree("parserx.g");
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        options.packageName = "mesut.parserx.parser2";
        tree.options = options;

        RecDescent rec = new RecDescent(tree);
        rec.gen();
    }

    @Test
    public void math() throws IOException {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        Factor.debug = true;
        /*Name.autoEncode = false;
        Factor.factorSequence = false;
        new Factor(tree).factorize();
        tree.printRules();*/
        new RecDescent(tree).gen();
    }

    @Test
    public void math2() throws IOException {
        //Name.autoEncode = true;
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        //Tree tree = Env.tree("factor/math.g");
        tree.options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/ll1";
        tree.options.packageName = "ll1";
        new RecDescent(tree).gen();
    }


    @Test
    public void mathReal() throws Exception {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        DescTester.check(tree, "var", "abc");
    }

    @Test
    public void recursion() throws Exception {
        Factor.factorSequence = false;
        Factor.debug = true;
        //Tree tree = Env.tree("rec/direct.g");
        //Tree tree = Env.tree("rec/direct2.g");
        //Tree tree = Env.tree("rec/indirect.g");
        //Tree tree = Env.tree("rec/expr.g");
        Tree tree = Env.tree("rec/cyc1.g");
        //new Recursion(tree).all();
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        tree.options = options;
        RecDescent rec = new RecDescent(tree);
        rec.gen();
    }

    @Test
    public void recursionAll() throws Exception {
        Factor.debug = true;
//        DescTester.check(Env.tree("rec/direct.g"), "A", "c", "ca", "cb", "caa", "cab", "cba", "cbb");
//        DescTester.check(Env.tree("rec/direct2.g"), "A", "c", "cb", "cca", "ccba");
//        DescTester.check(Env.tree("rec/direct-double.g"), "A", "b", "bba", "bbbaa");
        //DescTester.check(Env.tree("rec/indirect.g"), "A", "d", "aec", "ec", "adbc", "dbc");
        DescTester.check(Env.tree("rec/cyc1.g"), "A", "c", "db", "cab", "dbab");
        DescTester.check(Env.tree("rec/cyc1.g"), "B", "d", "ca", "dba");
    }
}
