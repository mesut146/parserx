package parser;

import common.Env;
import mesut.parserx.gen.lldfa.LLDFAGen;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.gen.lldfa.LaFinder;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LLDfaTest {

    void dot(LLDfaBuilder b) throws IOException {
        File file = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dot"));
        b.dot(new PrintWriter(new FileWriter(file)));
        try {
            Runtime.getRuntime().exec("dot -Tpng -O " + file).waitFor();
            //Thread.sleep(100);
            file.delete();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void single(String path) throws IOException {
        System.out.println("------------------------------------");
        Tree tree = Env.tree(path);
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.factor();
        dot(builder);
    }

    @Test
    public void all() throws IOException {
        File dir = new File(Env.dir, "src/test/resources/lldfa");
        for (String s : dir.list()) {
            if (s.equals("greedy-loop.g")) continue;
            if (s.equals("greedy-loop2.g")) continue;
            single("lldfa/" + s);
        }
    }

    @Test
    public void dfa() throws Exception {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //LLDFAGen.gen(tree, "java");
        DescTester.check2(Builder.tree(tree).rule("tree").input("E: a b c;", ""));
    }

    @Test
    public void computeLa() {
        Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        System.out.println(LaFinder.computeLa(new Name("regex"), tree));
    }

    @Test
    public void single2() throws IOException {
        //String path = "lldfa/rr-loop.g";
        String path = "lldfa/simple.g";
        //String path = "lldfa/greedy-opt.g";
        single(path);
        Tree tree = Env.tree(path);
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LLDFAGen.gen(tree, "java");
    }

    @Test
    public void sr() throws Exception {
        DescTester.check(Builder.tree("lldfa/sr.g").rule("E").
                input("abcdx", "E#1{A{'a'}, C#1{'b'}, 'c', 'd', 'x'}").
                input("aecdx", "E#1{A{'a'}, C#2{'e'}, 'c', 'd', 'x'}").
                input("abcdy", "E#2{'a', B{'b'}, 'c', 'd', 'y'}"));

        DescTester.check(Builder.tree("lldfa/sr2.g").rule("E").
                input("abcx", "E#1{A{'a', 'b'}, 'c', 'x'}").
                input("abcy", "E#2{B{'a', 'b', 'c'}, 'y'}"));

        DescTester.check(Builder.tree("lldfa/sr-loop.g").rule("E").
                input("abcx", "E#1{[A{C{'a', 'b'}, 'c'}], 'x'}").
                input("abcabcx", "E#1{[A{C{'a', 'b'}, 'c'}, A{C{'a', 'b'}, 'c'}], 'x'}").
                input("abcy", "E#2{[B{'a', 'b', 'c'}], 'y'}").
                input("abcabcy", "E#2{[B{'a', 'b', 'c'}, B{'a', 'b', 'c'}], 'y'}"));
    }

    @Test
    public void greedy() throws Exception {
//        DescTester.check2(Builder.tree("lldfa/greedy-opt.g").rule("E").
//                input("ax", "").
//                input("aax", ""));
        DescTester.check2(Builder.tree("lldfa/greedy-opt2.g").rule("E").
                input("cya", "").
                input("cyaebda", ""));

        single("lldfa/greedy-opt2.g");
    }

    @Test
    public void multi() throws Exception {
        DescTester.check2(Builder.tree("lldfa/multi-sym.g").rule("E").
                input("ax", "E#1{'a', 'x'}").
                input("ay", "E#2{'a', 'y'}").
                input("zpt", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}]}").
                input("zptc", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}], 'c'}"));
    }

    @Test
    public void rr_loop() throws Exception {
        DescTester.check2(Builder.tree("lldfa/simple.g").rule("E").
                input("acx", "E#1{A#1{'a'}, B#1{'c'}, 'x'}").
                input("adx", "E#1{A#1{'a'}, B#2{'d'}, 'x'}").
                input("bcx", "E#1{A#2{'b'}, B#1{'c'}, 'x'}").
                input("bdx", "E#1{A#2{'b'}, B#2{'d'}, 'x'}").
                input("acy", "E#2{A#1{'a'}, D#1{'c'}, 'y'}").
                input("ady", "E#2{A#1{'a'}, D#2{'d'}, 'y'}").
                input("bcy", "E#2{A#2{'b'}, D#1{'c'}, 'y'}").
                input("bdy", "E#2{A#2{'b'}, D#2{'d'}, 'y'}"));
        DescTester.check2(Builder.tree("lldfa/rr-loop.g").rule("E").
                input("x", "E#1{'x'}").
                input("y", "E#2{'y'}").
                input("ax", "E#1{[A#1{'a'}], 'x'}").
                input("ababx", "E#1{[A#1{'a'}, A#2{'b'}, A#1{'a'}, A#2{'b'}], 'x'}").
                input("ay", "E#2{[B#1{'a'}], 'y'}").
                input("bbaay", "E#2{[B#2{'b'}, B#2{'b'}, B#1{'a'}, B#1{'a'}], 'y'}"));
        DescTester.check2(Builder.tree("lldfa/rr-loop.g").rule("F").
                input("x", "F#1{X{'x'}}").
                input("y", "F#2{Y{'y'}}").
                input("ax", "F#1{X{[A#1{'a'}], 'x'}}").
                input("ababx", "F#1{X{[A#1{'a'}, A#2{'b'}, A#1{'a'}, A#2{'b'}], 'x'}}").
                input("ay", "F#2{Y{[B#1{'a'}], 'y'}}").
                input("bbaay", "F#2{Y{[B#2{'b'}, B#2{'b'}, B#1{'a'}, B#1{'a'}], 'y'}}"));
        DescTester.check2(Builder.tree("lldfa/rr-loop2.g").rule("E").
                input("acx", "E#1{[A#1{'a'}], [B#1{'c'}], 'x'}").
                input("bdx", "E#1{[A#2{'b'}], [B#2{'d'}], 'x'}").
                input("abcdx", "E#1{[A#1{'a'}, A#2{'b'}], [B#1{'c'}, B#2{'d'}], 'x'}").
                input("acy", "E#2{[C#1{'a'}], [D#1{'c'}], 'y'}").
                input("bdy", "E#2{[C#2{'b'}], [D#2{'d'}], 'y'}").
                input("abcdy", "E#2{[C#1{'a'}, C#2{'b'}], [D#1{'c'}, D#2{'d'}], 'y'}"));
        DescTester.check2(Builder.tree("lldfa/rr-loop-len2.g").rule("E").
                input("x", "E#1{'x'}").
                input("y", "E#2{'y'}").
                input("abx", "E#1{[A#1{'a', 'b'}], 'x'}").
                input("abcdx", "E#1{[A#1{'a', 'b'}, A#2{'c', 'd'}], 'x'}").
                input("aby", "E#2{[B#1{'a', 'b'}], 'y'}").
                input("abcdy", "E#2{[B#1{'a', 'b'}, B#2{'c', 'd'}], 'y'}").
                rule("F").
                input("x", "F#1{X{'x'}}").
                input("y", "F#2{Y{'y'}}").
                input("abx", "F#1{X{[A#1{'a', 'b'}], 'x'}}").
                input("abcdx", "F#1{X{[A#1{'a', 'b'}, A#2{'c', 'd'}], 'x'}}").
                input("aby", "F#2{Y{[B#1{'a', 'b'}], 'y'}}").
                input("abcdy", "F#2{Y{[B#1{'a', 'b'}, B#2{'c', 'd'}], 'y'}}")
        );
        DescTester.check2(Builder.tree("lldfa/rr-loop2-len2.g").rule("E").
                input("abefx", "E#1{A#1{'a', 'b'}, B#1{'e', 'f'}, 'x'}").
                input("abcdefghx", "E#1{A#1{'a', 'b'}, [A#2{'c', 'd'}], B#1{'e', 'f'}, [B#2{'g', 'h'}], 'x'}").
                input("abefy", "E#2{C#1{'a', 'b'}, D#1{'e', 'f'}, 'y'}").
                input("abcdefghy", "E#2{C#1{'a', 'b'}, [C#2{'c', 'd'}], D#1{'e', 'f'}, [D#2{'g', 'h'}], 'y'}"));
        DescTester.check(Builder.tree("lldfa/rr-loop-x.g").rule("E").
                input("x", "E#1{'x'}").
                input("y", "E#2{'y'}").
                input("abacx", "E#1{[A{'a', B#1{'b'}}, A{'a', B#2{'c'}}], 'x'}").
                input("abacy", "E#2{[C{'a', D#1{'b'}}, C{'a', D#2{'c'}}], 'y'}"));
        DescTester.check(Builder.tree("lldfa/rr-loop-deep.g").rule("E").
                input("x", "E#1{'x'}").
                input("acbdx", "E#1{[A#1{C#1{'a'}}, A#1{C#2{'c'}}, A#2{D#1{'b'}}, A#2{D#2{'d'}}], 'x'}").
                input("y", "E#2{'y'}").
                input("acbdy", "E#2{[B#1{K#1{'a'}}, B#1{K#2{'c'}}, B#2{M#1{'b'}}, B#2{M#2{'d'}}], 'y'}")
        );
        DescTester.check2(Builder.tree("lldfa/rr-loop-sub.g").rule("F").
                input("z", "F#2{'z'}").
                input("aaz", "F#2{['a', 'a'], 'z'}").
                input("x", "F#1{E#1{'x'}}").
                input("y", "F#1{E#2{'y'}}").
                input("ax", "F#1{E#1{[A#1{'a'}], 'x'}}").
                input("aby", "F#1{E#2{[B#1{'a'}, B#2{'b'}], 'y'}}"));
    }
}
