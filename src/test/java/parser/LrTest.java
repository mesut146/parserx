package parser;

import common.Env;
import mesut.parserx.gen.lr.LrCodeGen;
import mesut.parserx.gen.lr.LrDFAGen;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LrTest {

    public static void dots(LrDFAGen gen, String name) throws IOException {
        File dot = Env.dotFile(Utils.newName(name, ".dot"));
        gen.writeDot(new PrintWriter(dot));
        Utils.runDot(dot);

        File table = Env.dotFile(Utils.newName(name, "-table.dot"));
        gen.writeTableDot(new PrintWriter(table));
        Utils.runDot(table);
    }

    @Test
    public void codeGen() throws Exception {
        Tree tree = Env.tree("lr1/lr1.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LrCodeGen codeGen = new LrCodeGen(tree, LrType.LR1);
        codeGen.gen();
        dots(codeGen.gen, tree.file.getName());
    }


    @Test
    @Ignore
    public void itself() throws Exception {
        var path = new File("./src/main/grammar/parserx.g");
        Tree tree=Tree.makeTree(path);
        tree.options.outDir=Env.dotDir().getAbsolutePath();
        Builder.tree(tree)
                .dump()
                .file(path.getAbsolutePath())
                .lr(LrType.LR1);
    }


    @Test
    public void all() throws Exception {
        Builder.tree("lr1/lr1.g")
                .input("aea", "S#1{'a', E{'e'}, 'a'}")
                .input("beb", "S#2{'b', E{'e'}, 'b'}")
                .input("aeb", "S#3{'a', F{'e'}, 'b'}")
                .input("bea", "S#4{'b', F{'e'}, 'a'}")
                .lr();
        Builder.tree("lr1/left.g")
                .input("x", "E#3{'x'}")
                .input("xab", "E#1{E#3{'x'}, 'a', 'b'}")
                .input("xabac", "E#2{E#1{E#3{'x'}, 'a', 'b'}, 'a', 'c'}")
                .lr();
        Builder.tree("lr1/la.g")
                .input("bb", "E{B#2{'b'}, B#2{'b'}}")
                .input("abab", "E{B#1{'a', B#2{'b'}}, B#1{'a', B#2{'b'}}}")
                .input("aabaaab", "E{B#1{'a', B#1{'a', B#2{'b'}}}, B#1{'a', B#1{'a', B#1{'a', B#2{'b'}}}}}")
                .lr();
        Builder.tree("lr1/assoc.g")
                .input("1+2", "E#2{E#1{'1'}, '+', E#1{'2'}}")
                .input("1+2+3", "E#2{E#2{E#1{'1'}, '+', E#1{'2'}}, '+', E#1{'3'}}")
                .input("1*2*3", "E#3{E#1{'1'}, '*', E#3{E#1{'2'}, '*', E#1{'3'}}}")
                .lr();
        Builder.tree("lr1/assoc_bug.g")
                .dump()
                .input("1*2/3", "E#2{E#2{E#1{'1'}, op2#1{'*'}, E#1{'2'}}, op2#2{'/'}, E#1{'3'}}")
                .input("1/2*3", "E#2{E#2{E#1{'1'}, op2#2{'/'}, E#1{'2'}}, op2#1{'*'}, E#1{'3'}}")
                .input("1+2+3", "E#3{E#1{'1'}, op1#1{'+'}, E#3{E#1{'2'}, op1#1{'+'}, E#1{'3'}}}")
                .input("1+2-3", "E#3{E#1{'1'}, op1#1{'+'}, E#3{E#1{'2'}, op1#2{'-'}, E#1{'3'}}}")
                .input("1-2+3", "E#3{E#1{'1'}, op1#2{'-'}, E#3{E#1{'2'}, op1#1{'+'}, E#1{'3'}}}")
                .lr();
        Builder.tree("lr1/assoc2.g")
                //noassoc
                .input("1?2:3", "E#2{E#1{'1'}, '?', E#1{'2'}, ':', E#1{'3'}}")
                //left
                .input("1?2:3?4:5", "E#2{E#2{E#1{'1'}, '?', E#1{'2'}, ':', E#1{'3'}}, '?', E#1{'4'}, ':', E#1{'5'}}")
                //mid
                .input("1?2?3:4:5", "E#2{E#1{'1'}, '?', E#2{E#1{'2'}, '?', E#1{'3'}, ':', E#1{'4'}}, ':', E#1{'5'}}")
                //right
                .input("x1?2:3?4:5x", "E#3{'x', F#2{F#1{'1'}, '?', F#1{'2'}, ':', F#2{F#1{'3'}, '?', F#1{'4'}, ':', F#1{'5'}}}, 'x'}")
                .lr();
        Builder.tree("lr1/prec.g")
                .dump()
                .input("1+2*3", "E#4{E#7{'1'}, Eg2#1{'+'}, E#3{E#7{'2'}, Eg1#1{'*'}, E#7{'3'}}}")
                .input("2*3+1", "E#4{E#3{E#7{'2'}, Eg1#1{'*'}, E#7{'3'}}, Eg2#1{'+'}, E#7{'1'}}")
                .input("2^3*5+1", "E#4{E#3{E#1{E#7{'2'}, '^', E#7{'3'}}, Eg1#1{'*'}, E#7{'5'}}, Eg2#1{'+'}, E#7{'1'}}")
                .input("-2^3", "E#2{'-', E#1{E#7{'2'}, '^', E#7{'3'}}}")
                .input("1?2:3", "E#5{E#7{'1'}, '?', E#7{'2'}, ':', E#7{'3'}}")
                .input("1?2^3:4", "E#5{E#7{'1'}, '?', E#1{E#7{'2'}, '^', E#7{'3'}}, ':', E#7{'4'}}")
                .input("2*(1+2)^(-3)", "E#3{E#7{'2'}, Eg1#1{'*'}, E#1{E#6{'(', E#4{E#7{'1'}, Eg2#1{'+'}, E#7{'2'}}, ')'}, '^', E#6{'(', E#2{'-', E#7{'3'}}, ')'}}}")
                .lr();

        Builder.tree("lr1/prec-unary.g")
                .input("-1+3", "E#3{E#2{'-', E#1{'1'}}, '+', E#1{'3'}}")
                .input("1+-6", "E#3{E#1{'1'}, '+', E#2{'-', E#1{'6'}}}")
                .lr();
        Builder.tree("lr1/prec2.g")
                //call
                .input("a.b().c()", "E#1{E#1{E#18{prim#2{'a'}}, '.', call{'b', '(', ')'}}, '.', call{'c', '(', ')'}}")
                .input("a.b.c()", "E#1{E#2{E#18{prim#2{'a'}}, '.', 'b'}, '.', call{'c', '(', ')'}}")
                .input("a[b].c()", "E#1{E#3{E#18{prim#2{'a'}}, '[', E#18{prim#2{'b'}}, ']'}, '.', call{'c', '(', ')'}}")
                //access
                .input("a.b.c", "E#2{E#2{E#18{prim#2{'a'}}, '.', 'b'}, '.', 'c'}")
                .input("a.b().c", "E#2{E#1{E#18{prim#2{'a'}}, '.', call{'b', '(', ')'}}, '.', 'c'}")
                .input("a[b].c", "E#2{E#3{E#18{prim#2{'a'}}, '[', E#18{prim#2{'b'}}, ']'}, '.', 'c'}")
                //array access
                .input("a[b][c]", "E#3{E#3{E#18{prim#2{'a'}}, '[', E#18{prim#2{'b'}}, ']'}, '[', E#18{prim#2{'c'}}, ']'}")
                .input("a.b[c]", "E#3{E#2{E#18{prim#2{'a'}}, '.', 'b'}, '[', E#18{prim#2{'c'}}, ']'}")
                .input("a.b()[c]", "E#3{E#1{E#18{prim#2{'a'}}, '.', call{'b', '(', ')'}}, '[', E#18{prim#2{'c'}}, ']'}")
                //
                .input("m=(l=k?(j||(i&&(h|(g^(f&(e==(d<(c<<(a+((--(a++))*b)))))))))):l)"
                        .replace("(", "")
                        .replace(")", ""), "E#17{E#18{prim#2{'m'}}, assign_op#1{'='}, E#17{E#18{prim#2{'l'}}, assign_op#1{'='}, E#16{E#18{prim#2{'k'}}, '?', E#15{E#18{prim#2{'j'}}, '||', E#14{E#18{prim#2{'i'}}, '&&', E#13{E#18{prim#2{'h'}}, '|', E#12{E#18{prim#2{'g'}}, '^', E#11{E#18{prim#2{'f'}}, '&', E#10{E#18{prim#2{'e'}}, Eg7#1{'=='}, E#9{E#18{prim#2{'d'}}, Eg6#1{'<'}, E#8{E#18{prim#2{'c'}}, Eg5#1{'<<'}, E#7{E#18{prim#2{'a'}}, Eg4#1{'+'}, E#6{E#5{Eg2#4{'--'}, E#4{E#18{prim#2{'a'}}, Eg1#1{'++'}}}, Eg3#1{'*'}, E#18{prim#2{'b'}}}}}}}}}}}}, ':', E#18{prim#2{'l'}}}}}")
                .lr();
        Builder.tree("lr1/calc.g")
                .input("1+2", "E#6{E#2{'1'}, Eg2#1{'+'}, E#2{'2'}}")
                .input("1*2", "E#5{E#2{'1'}, Eg1#1{'*'}, E#2{'2'}}")
                .input("1+2*3", "E#6{E#2{'1'}, Eg2#1{'+'}, E#5{E#2{'2'}, Eg1#1{'*'}, E#2{'3'}}}")
                .input("2*3+1", "E#6{E#5{E#2{'2'}, Eg1#1{'*'}, E#2{'3'}}, Eg2#1{'+'}, E#2{'1'}}")
                .input("1+2^3", "E#6{E#2{'1'}, Eg2#1{'+'}, E#3{E#2{'2'}, '^', E#2{'3'}}}")
                .input("2*2^-3", "E#5{E#2{'2'}, Eg1#1{'*'}, E#3{E#2{'2'}, '^', E#4{'-', E#2{'3'}}}}")
                .lr();
        Builder.tree("lr1/factor-loop-right.g")
                .input("ac", "E#1{A#2{'a'}, 'c'}")
                .input("ab", "E#2{B#2{'a'}, 'b'}")
                .input("aac", "E#1{A#1{'a', A#2{'a'}}, 'c'}")
                .input("aab", "E#2{B#1{'a', B#2{'a'}}, 'b'}")
                .lr();
        Builder.tree("lr1/rec.g")
                .input("abc", "E#1{A#2{B{'a', 'b'}}, 'c'}")
                .input("abd", "E#2{'a', 'b', 'd'}")
                .input("ababc", "E#1{A#1{A#2{B{'a', 'b'}}, B{'a', 'b'}}, 'c'}")
                .lr();

        Builder.tree("lr1/la2.g")
                .input("aax", "E#1{A{'a', B{'a'}}, 'x'}")
                .input("baay", "E#2{'b', A{'a', B{'a'}}, 'y'}")
                .lr();

    }

    @Test
    public void eps() throws Exception {
        Builder.tree("lr1/regex.g")
                .dump()
                .input("ccc", "E#4{c+{'c', 'c', 'c'}}")
                .input("bbbccc", "E#3{b+{'b', 'b', 'b'}, c+{'c', 'c', 'c'}}")
                .input("accc", "E#2{'a', c+{'c', 'c', 'c'}}")
                .input("abbbccc", "E#1{'a', b+{'b', 'b', 'b'}, c+{'c', 'c', 'c'}}")
                .lr();
        Builder.tree("lr1/eps.g")
                .input("x", "E#1{A#2{}, B#2{}, 'x'}")
                .input("ax", "E#1{A#1{'a'}, B#2{}, 'x'}")
                .input("bx", "E#1{A#2{}, B#1{'b'}, 'x'}")
                .input("abx", "E#1{A#1{'a'}, B#1{'b'}, 'x'}")
                .input("cy", "E#2{'c', C{}, 'y'}")
                .lr();
        Builder.tree("lr1/sr.g")
                .dump()
                .input("abc","E#1{'a', 'b', 'c'}")
                .input("bc","E#2{'b', 'c'}")
                .input("bd","E#3{'b', 'd'}")
                .lr();
    }
}
