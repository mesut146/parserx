package parser;

import common.Env;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.ast.AstGen;
import mesut.parserx.gen.lldfa.*;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class LLDfaTest {

    @Test
    public void firstSet() throws IOException {
        Tree tree = Env.tree("firstSet.g");
        FirstSet firstSet = new FirstSet(tree);

        var setE =firstSet.firstSetSorted(new Name("E"), true);
        Assert.assertEquals("[a, y, E]",setE.toString());

        var setA = firstSet.firstSetSorted(new Name("A"), true);
        Assert.assertEquals("[a, b, c, d, e, y, A, B, E]",setA.toString());

        var setB = firstSet.firstSetSorted(new Name("B"), true);
        Assert.assertEquals("[a, e, y, E]",setB.toString());
    }

    @Test
    public void computeLa() throws IOException {
        var tree = Env.tree("lldfa/la_test.g");
        var actual = new ArrayList<>(LaFinder.computeLa(new Name("B"), tree));
        Collections.sort(actual);
        Assert.assertEquals("[a, b, c, d, y]", actual.toString());
    }


    @Test
    @Ignore
    public void java() throws Exception {
        //todo
        Builder.tree("java/JavaParser.g")
                .file(Env.getResFile("java/a.java.res").getAbsolutePath())
                .checkCC();
    }


    @Test
    public void splitter() throws IOException {
        Tree tree = Env.tree("prefix.g");
        var split = Splitter.split(new Name("B"), tree);
        System.out.printf("left: %s, right: %s, mid: %s", split.isLeft(tree), split.isRight(tree), split.isMid());
    }


    @Test
    public void token_stream() throws Exception {
        Builder.tree("lldfa/token_stream.g")
                .rule("A")
                .input("abc", "A#1{'a', 'b', 'c'}")
                .input("abd", "A#2{'a', 'b', 'd'}")
                .rule("E")
                .input("abcx", "E#1{A#1{'a', 'b', 'c'}, 'x'}")
                .input("abfy", "E#2{B#2{'a', 'b', 'f'}, 'y'}")
                .check();
    }

    @Test
    public void left_dump() throws IOException {
        Tree tree=Env.tree("lldfa/left-indirect3.g");
        AstGen.gen(tree, Lang.JAVA);
        new RecursionHandler(tree).handleAll();
        RecursionHandler.clearArgs(tree);
        tree.printRules();
    }

    @Test
    public void leftRec() throws Exception {
        Builder.tree("lldfa/left.g")
                .rule("A")
                .input("c", "A#3{'c'}")
                .input("ca", "A#1{A#3{'c'}, 'a'}")
                .input("cb", "A#2{A#3{'c'}, 'b'}")
                .input("caa", "A#1{A#1{A#3{'c'}, 'a'}, 'a'}")
                .input("cab", "A#2{A#1{A#3{'c'}, 'a'}, 'b'}")
                .rule("B")
                .input("caa", "B#1{B#1{B#2{'c'}, 'a'}, 'a'}")
                .input("bbbcaa", "B#1{B#1{B#2{['b', 'b', 'b'], 'c'}, 'a'}, 'a'}")
                .input("bbbdaa", "B#1{B#1{B#3{['b', 'b', 'b'], 'd'}, 'a'}, 'a'}")
                .rule("C")
                .input("xb", "C#1{C#3{'x'}, 'b'}")
                .input("xaaab", "C#1{C#3{'x'}, ['a', 'a', 'a'], 'b'}")
                .input("xaaac", "C#2{C#3{'x'}, ['a', 'a', 'a'], 'c'}")
                .checkCC();
        Builder.tree("lldfa/left-indirect3.g")
                .rule("A")
                .input("x", "A#5{'x'}")
                .input("xa", "A#1{A#5{'x'}, 'a'}")
                .input("xadb", "A#3{B#1{A#1{A#5{'x'}, 'a'}, 'd'}, 'b'}")
                .input("yhfbp", "A#2{A#3{B#3{C#2{B#4{'y'}, 'h'}, 'f'}, 'b'}, 'p'}")
                .checkCC();
        Builder.tree("lldfa/left-indirect-long.g")
                .rule("A")
                .input("ya", "A#1{B#2{'y'}, 'a'}")
                .input("zca", "A#1{B#1{C#2{'z'}, 'c'}, 'a'}")
                .input("tdca", "A#1{B#1{C#1{D#2{'t'}, 'd'}, 'c'}, 'a'}")
                .input("xedca", "A#1{B#1{C#1{D#1{A#2{'x'}, 'e'}, 'd'}, 'c'}, 'a'}")
                .check();
        Builder.tree("lldfa/left-indirect2.g")
                .rule("A")
                .input("xdbdb", "A#2{B#1{A#2{B#1{A#3{'x'}, 'd'}, 'b'}, 'd'}, 'b'}")
                .rule("B")
                .input("ybad", "B#1{A#1{A#2{B#2{'y'}, 'b'}, 'a'}, 'd'}")
                .check();
    }

    @Test
    public void rightRec() throws Exception {
        Builder.tree("lldfa/right_factor_alt.g")
                .rule("E")
                .input("y", "E#3{'y'}")
                .input("ax", "E#2{'a', 'x'}")
                .input("ay", "E#1{'a', E#3{'y'}}")
                .input("aax", "E#1{'a', E#2{'a', 'x'}}")
                .input("aay", "E#1{'a', E#1{'a', E#3{'y'}}}")
                .check();
        Builder.tree("lldfa/right_as_factor.g")
                .rule("A")
                .input("aax", "A#1{['a', 'a'], 'x'}")
                .input("by", "A#2{B#2{'b'}, 'y'}")
                .input("aby", "A#2{B#1{'a', B#2{'b'}}, 'y'}")
                .input("aaby", "A#2{B#1{'a', B#1{'a', B#2{'b'}}}, 'y'}")
                .check();
    }

    @Test
    public void mid() throws Exception {
        Builder.tree("lldfa/mid_as_factor.g")
                .rule("E")
                .input("acedbx", "E#1{A#1{'a', A#2{'c', A#3{'e'}, 'd'}, 'b'}, 'x'}")
                .input("acy", "E#2{[X#1{'a'}, X#2{'c'}], 'y'}")
                .check();

        Builder.tree("lldfa/mid.g")
                .dump()
                .rule("E")
                .input("cx", "E#1{A#2{'c'}, 'x'}")
                .input("acbx", "E#1{A#1{'a', A#2{'c'}, 'b'}, 'x'}")
                .input("aacbbx", "E#1{A#1{'a', A#1{'a', A#2{'c'}, 'b'}, 'b'}, 'x'}")
                .input("aaay", "E#2{['a', 'a', 'a'], 'y'}")
                .check();
//        Builder.tree("lldfa/mid_alt_factor.g")
//                .dump()
//                .rule("F")
//                .input("aadbc", "")
//                .check();
    }


    @Test
    @Ignore
    public void greedy() throws Exception {
//        DescTester.check2(Builder.tree("lldfa/greedy-opt.g").rule("E")
//                .input("ax", "")
//                .input("aax", ""));
        Builder.tree("lldfa/greedy-opt2.g").rule("E")
                        .dump()
                .input("cya", "")
        .input("cyaebda", "")
                .check();

    }

    @Test
    public void all() throws Exception {
        Builder.tree("lldfa/pre-reduce.g").rule("E")
        .input("ax", "E#1{'a', 'x'}")
        . input("azy", "E#2{A{'a', 'z'}, 'y'}")
                .check();
        Builder.tree("lldfa/cc-complex.g").rule("E")
                .input("ab", "E#1{'a', S1#1{'b'}}")
                .input("acd", "E#1{'a', S1#2{'c', S3#2{'d'}}}")
                .input("aceb", "E#1{'a', S1#2{'c', S3#1{'e', S1#1{'b'}}}}")
                .input("acey", "E#2{'a', 'c', 'e', 'y'}")
                .check();
        astSimple();
        rr_loop();
        multi();
        sr();
        Builder.tree("lldfa/expand.g").rule("E")
                .dump()
                .input("ax", "E#1{A#1{'a'}, 'x'}")
                .input("ay", "E#2{A#1{'a'}, 'y'}")
                .input("bx", "E#1{A#2{'b'}, 'x'}")
                .input("by", "E#2{A#2{'b'}, 'y'}")
                .check();
    }

    @Test
    public void action() throws Exception {
        Builder.tree("lldfa/action.g")
                .rule("E")
                .dump()
                .input("ab", "E{'a', 'b'}")
                .check();
    }

    @Test
    public void astSimple() throws Exception {
        Builder.tree("lldfa/ast.g").rule("A")
                .dump()
                .input("a", "A#1{'a'}")
                .input("ab", "A#2{'a', 'b'}")
                .input("abc", "A#3{'a', 'b', 'c'}")
                .check();
        Builder.tree("lldfa/simple.g").rule("E").dump()
                .input("acx", "E#1{A#1{'a'}, B#1{'c'}, 'x'}")
                .input("adx", "E#1{A#1{'a'}, B#2{'d'}, 'x'}")
                .input("bcx", "E#1{A#2{'b'}, B#1{'c'}, 'x'}")
                .input("bdx", "E#1{A#2{'b'}, B#2{'d'}, 'x'}")
                .input("acy", "E#2{A#1{'a'}, D#1{'c'}, 'y'}")
                .input("ady", "E#2{A#1{'a'}, D#2{'d'}, 'y'}")
                .input("bcy", "E#2{A#2{'b'}, D#1{'c'}, 'y'}")
                .input("bdy", "E#2{A#2{'b'}, D#2{'d'}, 'y'}")
                .check();
    }

    @Test
    public void sr() throws Exception {
        Builder.tree("lldfa/sr.g").rule("E")
                .dump()
                .input("abcdx", "E#1{A{'a'}, C#1{'b'}, 'c', 'd', 'x'}")
                .input("aecdx", "E#1{A{'a'}, C#2{'e'}, 'c', 'd', 'x'}")
                .input("abcdy", "E#2{'a', B{'b'}, 'c', 'd', 'y'}")
                .check();

        Builder.tree("lldfa/sr2.g").rule("E")
                .input("abcx", "E#1{A{'a', 'b'}, 'c', 'x'}")
                .input("abcy", "E#2{B{'a', 'b', 'c'}, 'y'}")
                .check();

        Builder.tree("lldfa/sr-loop.g").rule("E")
                .dump()
                .input("abcx", "E#1{[A{C{'a', 'b'}, 'c'}], 'x'}")
                .input("abcabcx", "E#1{[A{C{'a', 'b'}, 'c'}, A{C{'a', 'b'}, 'c'}], 'x'}")
                .input("abcy", "E#2{[B{'a', 'b', 'c'}], 'y'}")
                .input("abcabcy", "E#2{[B{'a', 'b', 'c'}, B{'a', 'b', 'c'}], 'y'}")
                .check();
    }

    @Test
    public void multi() throws Exception {
        Builder.tree("lldfa/multi-sym.g").dump()
                .rule("E")
                .input("ax", "E#1{'a', 'x'}")
                .input("ay", "E#2{'a', 'y'}")
                .input("zpt", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}]}")
                .input("zptc", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}], 'c'}")
                .rule("F")
                .input("abx", "F#1{'a', 'b', 'x'}")
                .input("b", "F#1{'b'}")
                .input("bx", "F#1{'b', 'x'}")
                .input("by", "F#2{'b', 'y'}")
                .check();
    }

    @Test
    public void rr_loop() throws Exception {
        Builder.tree("lldfa/rr-loop.g").rule("E")
                .dump()
                .input("x", "E#1{'x'}")
                .input("y", "E#2{'y'}")
                .input("ax", "E#1{[A#1{'a'}], 'x'}")
                .input("ababx", "E#1{[A#1{'a'}, A#2{'b'}, A#1{'a'}, A#2{'b'}], 'x'}")
                .input("ay", "E#2{[B#1{'a'}], 'y'}")
                .input("bbaay", "E#2{[B#2{'b'}, B#2{'b'}, B#1{'a'}, B#1{'a'}], 'y'}")
                .rule("F")
                .input("x", "F#1{X{'x'}}")
                .input("y", "F#2{Y{'y'}}")
                .input("ax", "F#1{X{[A#1{'a'}], 'x'}}")
                .input("ababx", "F#1{X{[A#1{'a'}, A#2{'b'}, A#1{'a'}, A#2{'b'}], 'x'}}")
                .input("ay", "F#2{Y{[B#1{'a'}], 'y'}}")
                .input("bbaay", "F#2{Y{[B#2{'b'}, B#2{'b'}, B#1{'a'}, B#1{'a'}], 'y'}}")
                .check();
        Builder.tree("lldfa/rr-loop2.g").rule("E").dump()
                .input("acx", "E#1{[A#1{'a'}], [B#1{'c'}], 'x'}")
                .input("bdx", "E#1{[A#2{'b'}], [B#2{'d'}], 'x'}")
                .input("abcdx", "E#1{[A#1{'a'}, A#2{'b'}], [B#1{'c'}, B#2{'d'}], 'x'}")
                .input("acy", "E#2{[C#1{'a'}], [D#1{'c'}], 'y'}")
                .input("bdy", "E#2{[C#2{'b'}], [D#2{'d'}], 'y'}")
                .input("abcdy", "E#2{[C#1{'a'}, C#2{'b'}], [D#1{'c'}, D#2{'d'}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-len2.g")
                .rule("E")
                .input("x", "E#1{'x'}")
                .input("y", "E#2{'y'}")
                .input("abx", "E#1{[A#1{'a', 'b'}], 'x'}")
                .input("abcdx", "E#1{[A#1{'a', 'b'}, A#2{'c', 'd'}], 'x'}")
                .input("aby", "E#2{[B#1{'a', 'b'}], 'y'}")
                .input("abcdy", "E#2{[B#1{'a', 'b'}, B#2{'c', 'd'}], 'y'}")
                .rule("F")
                .input("x", "F#1{X{'x'}}")
                .input("y", "F#2{Y{'y'}}")
                .input("abx", "F#1{X{[A#1{'a', 'b'}], 'x'}}")
                .input("abcdx", "F#1{X{[A#1{'a', 'b'}, A#2{'c', 'd'}], 'x'}}")
                .input("aby", "F#2{Y{[B#1{'a', 'b'}], 'y'}}")
                .input("abcdy", "F#2{Y{[B#1{'a', 'b'}, B#2{'c', 'd'}], 'y'}}")
                .check();
        Builder.tree("lldfa/rr-loop2-len2.g")
                .rule("E")
                .input("abefx", "E#1{A#1{'a', 'b'}, B#1{'e', 'f'}, 'x'}")
                .input("abcdefghx", "E#1{A#1{'a', 'b'}, [A#2{'c', 'd'}], B#1{'e', 'f'}, [B#2{'g', 'h'}], 'x'}")
                .input("abefy", "E#2{C#1{'a', 'b'}, D#1{'e', 'f'}, 'y'}")
                .input("abcdefghy", "E#2{C#1{'a', 'b'}, [C#2{'c', 'd'}], D#1{'e', 'f'}, [D#2{'g', 'h'}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-x.g")
                .rule("E")
                .input("x", "E#1{'x'}")
                .input("y", "E#2{'y'}")
                .input("abacx", "E#1{[A{'a', B#1{'b'}}, A{'a', B#2{'c'}}], 'x'}")
                .input("abacy", "E#2{[C{'a', D#1{'b'}}, C{'a', D#2{'c'}}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-deep.g")
                .rule("E")
                .input("x", "E#1{'x'}")
                .input("acbdx", "E#1{[A#1{C#1{'a'}}, A#1{C#2{'c'}}, A#2{D#1{'b'}}, A#2{D#2{'d'}}], 'x'}")
                .input("y", "E#2{'y'}")
                .input("acbdy", "E#2{[B#1{K#1{'a'}}, B#1{K#2{'c'}}, B#2{M#1{'b'}}, B#2{M#2{'d'}}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-sub.g")
                .rule("F")
                .input("z", "F#2{'z'}")
                .input("aaz", "F#2{['a', 'a'], 'z'}")
                .input("x", "F#1{E#1{'x'}}")
                .input("y", "F#1{E#2{'y'}}")
                .input("ax", "F#1{E#1{[A#1{'a'}], 'x'}}")
                .input("aby", "F#1{E#2{[B#1{'a'}, B#2{'b'}], 'y'}}")
                .check();
    }
}
