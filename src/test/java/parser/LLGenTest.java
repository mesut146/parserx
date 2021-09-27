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
import org.junit.Test;

import java.io.File;

public class LLGenTest {

    @Test
    public void normalize() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Tree.makeTree(Env.getResFile("ll/norm.g"));
        tree.options = options;
        //new Normalizer(tree).normalize();
        //new Factor(tree).factorize();
        //System.out.println(tree);
        new RecDescent(tree).gen();
    }

    @Test
    public void printer() throws Exception {
        Tester.check(Env.tree("ll/norm.g"), "A", "ab", "cf", "ccf", "ddf", "ddcf");
    }

    @Test
    public void factorAll() throws Exception {
        Factor.debug = false;

        Tester.check(Env.tree("factor/single.g"), "A", "ac", "eb", "adb");
        Tester.check(Env.tree("factor/single2.g"), "A", "aac", "aadbeb");
        Tester.check(Env.tree("factor/group.g"), "A", "ab", "ace", "de");
        Tester.check(Env.tree("factor/list.g"), "A", "ab", "c", "ac", "aaaaaac");
        Tester.check(Env.tree("factor/group-list.g"), "A", "c", "abc", "ababc", "e", "ade");
        Tester.check(Env.tree("factor/eps.g"), "A", "a", "aa");
        Tester.check(Env.tree("factor/eps.g"), "B", "ab", "a", "aa");
        Tester.check(Env.tree("factor/double-same.g"), "A", "aab", "aac");
        Tester.check(Env.tree("factor/double-same.g"), "B", "abc", "abd");
        Tester.check(Env.tree("factor/double-same-extra.g"), "A", "aab", "c", "aadb", "axb", "eb");
        Tester.check(Env.tree("factor/double-same-extra2.g"), "B", "aad", "ax", "e");
        Tester.check(Env.tree("factor/double-same-extra2.g"), "A", "aab", "c", "aadb", "axb", "eb");
    }

    @Test
    public void eps() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("factor/eps.g"));
        System.out.println(new Epsilons(tree).trim(tree.getRule("C").rhs));
    }

    @Test
    public void factored() throws Exception {
        //Tree tree = Tree.makeTree(Env.getResFile("factor/double-same.g"));
        //Tree tree = Tree.makeTree(Env.getResFile("factor/double-same-extra.g"));
        Tree tree = Tree.makeTree(Env.getResFile("factor/double-same-extra2.g"));
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
        Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
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
        //Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        tree.options = options;
        VisitorGenerator visitorGenerator = new VisitorGenerator(tree);
        visitorGenerator.generate();
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
    public void parserx() throws Exception {
        Factor.factorSequence = true;
        Factor.debug = true;
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        Options options = new Options();
        //options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/test/java/itself";
        options.outDir = "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/java/mesut/parserx/parser";
        options.packageName = "mesut.parserx.parser";
        tree.options = options;

        RecDescent rec = new RecDescent(tree);
        rec.gen();
    }

    @Test
    public void recursionAll() throws Exception {
        Factor.debug = true;
        //Tester.check(Env.tree("rec/direct.g"), "A", "c","ca","cb","caa","cab","cba","cbb");
        //Tester.check(Env.tree("rec/direct2.g"), "A", "c", "cb", "cca", "ccba");
        //Tester.check(Env.tree("rec/direct-double.g"), "A", "b","bba","bbbaa");
        //Tester.check(Env.tree("rec/cyc1.g"), "A", "c","db","cab");
        Tester.check(Env.tree("rec/cyc1.g"), "B", "d", "ca", "dba");
    }
}
