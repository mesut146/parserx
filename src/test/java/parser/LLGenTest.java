package parser;

import common.Env;
import mesut.parserx.gen.*;
import mesut.parserx.gen.ll.RecDescent;
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
        //new Normalizer(tree).normalize();
        //new Factor(tree).factorize();
        //System.out.println(tree);
        new RecDescent(tree, options).gen();
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
        RecDescent gen = new RecDescent(tree, options);
        Name.debug = false;
        Factor.debug = true;
        gen.gen();
    }

    @Test
    public void all() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
        new LexerGenerator(tree, options).generate();
        RecDescent gen = new RecDescent(tree, options);
        gen.gen();
    }

    @Test
    public void visitor() throws Exception {
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        options.parserClass = "Test";
        //Tree tree = Tree.makeTree(Env.getResFile("calc-1.g"));
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"));
        VisitorGenerator visitorGenerator = new VisitorGenerator(tree, options);
        visitorGenerator.generate();
    }

    @Test
    public void recursion() throws Exception {
        Factor.factorSequence = false;
        Factor.debug = true;
        //Tree tree = Env.tree("rec/direct.g");
        Tree tree = Env.tree("rec/indirect.g");
        //new Recursion(tree).all();
        Options options = new Options();
        options.outDir = Env.dotDir().getAbsolutePath();
        RecDescent rec = new RecDescent(tree, options);
        rec.gen();
    }

    @Test
    public void recursionAll() throws Exception {
        Factor.debug = false;
        Tester.check(Env.tree("rec/direct.g"), "A", "c","ca","cb","caa","cab","cba","cbb");
    }
}
