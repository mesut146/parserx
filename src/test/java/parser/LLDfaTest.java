package parser;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.lldfa.LLDFAGen;
import mesut.parserx.gen.lldfa.LLDfaBuilder;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LLDfaTest {

    void dots(NFA nfa, File f) throws IOException {
        File file = Env.dotFile(Utils.newName(f.getName(), ".dot"));
        nfa.dot(new FileWriter(file));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    void dot(LLDfaBuilder b) throws IOException {
        File file = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dot"));
        b.dot(new PrintWriter(new FileWriter(file)));
        Runtime.getRuntime().exec("dot -Tpng -O " + file);
    }

    void single(String path) throws IOException {
        System.out.println("------------------------------------");
        Tree tree = Env.tree(path);
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.factor();
        //builder.normalize();
        dot(builder);
    }

    @Test
    public void all() throws IOException {
        File dir = new File(Env.dir, "src/test/resources/lldfa");
        for (String s : dir.list()) {
            single("lldfa/" + s);
        }
    }

    @Test
    public void dfa() throws IOException {
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/grammar/parserx.g"));
        //Tree tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        single("lldfa/mid.g");
        single("lldfa/mid2.g");
        single("lldfa/left.g");
        single("lldfa/left-indirect.g");
        single("lldfa/right.g");
        single("lldfa/right-indirect.g");

        single("lldfa/factor.g");
        single("lldfa/greedy.g");
        single("lldfa/greedy2.g");
        single("lldfa/rr-loop.g");
        single("lldfa/rr-loop-len2.g");
        single("lldfa/rr-loop-x.g");
        single("lldfa/rr-loop2.g");
        single("lldfa/rr-loop2-len2.g");
        single("lldfa/len2.g");
        single("lldfa/sr.g");
        single("lldfa/sr2.g");
        single("lldfa/rr.g");
        single("lldfa/rr-loop-sub.g");
        single("lldfa/rr-loop-rec.g");
        single("lldfa/sr-loop.g");
        //single("lldfa/rr2.g");

        single("lldfa/rr-loop-deep.g");
        single("lldfa/rr-loop-deep1.g");
        single("lldfa/rr-loop-deep2.g");
        single("lldfa/rr-loop-deep3.g");
        single("lldfa/rr-loop-deep4.g");
    }

    @Test
    public void single() throws IOException {
        single("lldfa/rr-loop2-len2.g");
        single("lldfa/rr-loop-x.g");
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
    public void real() throws Exception {
//        DescTester.check(Env.tree("lldfa/simple.g"), "E",
//                "acx", "adx", "bcx", "bdx",
//                "acy", "ady", "bcy", "bdy");
        DescTester.check(Builder.tree("lldfa/simple.g").rule("E").
                input("acx", "E#1{A#1{'a'}, B#1{'c'}, 'x'}").
                input("adx", "E#1{A#1{'a'}, B#2{'d'}, 'x'}").
                input("bcx","E#1{A#2{'b'}, B#1{'c'}, 'x'}").
                input("bdx","E#1{A#2{'b'}, B#2{'d'}, 'x'}").
                input("acy", "E#2{A#1{'a'}, B#1{'c'}, 'x'}").
                input("ady", "E#2{A#1{'a'}, B#2{'d'}, 'x'}").
                input("bcy","E#2{A#2{'b'}, B#1{'c'}, 'x'}").
                input("bdy","E#2{A#2{'b'}, B#2{'d'}, 'x'}"));
//        DescTester.check(Env.tree("lldfa/rr-loop.g"), "E",
//                "x", "y", "ax", "ababx",
//                "ay", "bbaay");
//        DescTester.check(Env.tree("lldfa/rr-loop.g"), "F",
//                "x", "y", "ax", "ababx",
//                "ay", "bbaay");
//        DescTester.check(Env.tree("lldfa/rr-loop2.g"), "E",
//                "acx", "bdx", "abcdx",
//                "acy", "bdy", "abcdy");
//        DescTester.check(Env.tree("lldfa/rr-loop-len2.g"), "E",
//                "x", "y", "abx", "abcdx",
//                "aby", "abcdy");
//        DescTester.check(Env.tree("lldfa/rr-loop-len2.g"), "F",
//                "x", "y", "abx", "abcdx",
//                "aby", "abcdy");
//        DescTester.check(Env.tree("lldfa/rr-loop2-len2.g"), "E",
//                "abefx", "abcdefghx", "abefy", "abcdefghy");
//        DescTester.check(Env.tree("lldfa/rr-loop-x.g"), "E",
//                "x", "y", "abacx", "abacy");
    }
}
