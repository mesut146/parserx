package parser;

import common.Env;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.lldfa.*;
import mesut.parserx.nodes.Name;
import mesut.parserx.nodes.Tree;
import mesut.parserx.utils.Utils;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

public class LLDfaTest {

    void dot(LLDfaBuilder b) throws IOException {
        var file = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dot"));
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
        var tree = Env.tree(path);
        var builder = new LLDfaBuilder(tree);
        builder.factor();
        dot(builder);
        dump(builder);
    }

    @Test
    public void allDot() throws IOException {
        single("rec/cyc.g");
//        File dir = new File("./src/test/resources/lldfa");
//        for (String s : dir.list()) {
//            if (s.startsWith("greedy")) continue;
//            single("lldfa/" + s);
//        }
    }

    void dump(LLDfaBuilder b) throws IOException {
        var file = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dump"));
        var file2 = Env.dotFile(Utils.newName(b.tree.file.getName(), ".dump2"));
        b.dump(new PrintWriter(new FileWriter(file)));
        b.dump2(new PrintWriter(new FileWriter(file2)));
    }

    @Test
    public void parserx() throws Exception {
        var tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //LLDFAGen.gen(tree, "java");
        Builder.tree(tree).rule("tree").input(Utils.read(tree.file), "").check();
        var builder = new LLDfaBuilder(tree);
        builder.factor();
        dump(builder);
    }

    @Test
    public void math() throws Exception {
        var tree = Tree.makeTree(new File("/media/mesut/SSD-DATA/IdeaProjects/math/grammar/math.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();

        Builder.tree(tree).rule("line").
                input("a+b*c", "").
                check();
        var builder = new LLDfaBuilder(tree);
        //builder.factor();
        dump(builder);
    }

    @Test
    public void computeLa() {
        var tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        System.out.println(LaFinder.computeLa(new Name("regex"), tree));
    }

    @Test
    public void blocks() {
        var tree = Tree.makeTree(new File("./doc/xml.g"));
        System.out.println(tree);
    }

    @Test
    public void itself() throws Exception {
        //Item.printLa = false;
        Tree tree = Tree.makeTree(new File("./src/main/grammar/parserx.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        //tree.options.packageName = "mesut.parserx.parser";
        //var input = Env.getResFile("lexer/xml-mode.g");
        var input = Env.getResFile("lexer/action.g");
        //var input = tree.file;
        var b = Builder.tree(tree).rule("tree").file(input.getAbsolutePath());
        //var b = Builder.tree(tree).rule("tree").file(Env.getResFile("lexer/member.g").getAbsolutePath());
        //var b = Builder.tree(tree).rule("tree").input("A: a+ - \"abc\";", "");
        DescTester.checkTokens(b);
        //ParserGen.gen(tree, Lang.JAVA);
    }

    @Test
    public void prefix() throws IOException {
        Tree tree = Env.tree("prefix.g");
        var split = Splitter.split(new Name("B"), tree);
        System.out.printf("left: %s, right: %s, mid: %s", split.isLeft(tree), split.isRight(tree), split.isMid());
    }

    @Test
    public void preReduce() throws Exception {
        Builder.tree("lldfa/pre-reduce.g").rule("E").
                input("ax", "E#1{'a', 'x'}").
                input("azy", "E#2{A{'a', 'z'}, 'y'}")
                .check();
    }

    @Test
    public void leftRec() throws Exception {
        //single("lldfa/left.g");
        single("lldfa/left-indirect2.g");
        single("lldfa/left-indirect3.g");
//        Builder.tree("lldfa/left.g").rule("E")
//                .input("b", "E#2{'b'}")
//                .input("ba", "")
//                .check();
//        Builder.tree("lldfa/left.g").rule("A")
//                .input("c", "A#3{'c'}")
//                .input("ca", "")
//                .input("cb", "")
//                .input("caa", "")
//                .input("cab", "")
//                .check();
//        Builder.tree("lldfa/left-indirect.g").rule("A")
//                .input("b", "A#2{'b'}")
//                .input("da", "A#1{B#2{'d'}, 'a'}")
//                //.input("bca","")
//                //.input("daca","")
//                //.input("bcaca","")
//                .check();
//        Builder.tree("lldfa/left-indirect2.g").rule("A")
//                .input("xdbdb", "")
//                .check();
    }

    @Test
    public void rightRec() throws Exception {
//        DescTester.check2(Builder.tree("lldfa/right.g").rule("E").
//                input("y", "E#3{'y'}").
//                input("ax", "E#2{'a', 'x'}").
//                input("ay", "E#1{'a', E#3{'y'}}").
//                input("aax", "E#1{'a', E#2{'a', 'x'}}"));
        Builder.tree("lldfa/right-factor.g").rule("A").
                input("aax", "A#1{['a', 'a'], 'x'}").
                input("by", "A#2{B#2{'b'}, 'y'}").
                input("aby", "A#2{B#1{'a', B#2{'b'}}, 'y'}").
                input("aaby", "A#2{B#1{'a', B#1{'a', B#2{'b'}}}, 'y'}")
                .check();
    }

    @Test
    public void mid() throws Exception {
//        Builder.tree("lldfa/mid3.g").rule("E").
//                input("acedbx", "").check();
//
//        Builder.tree("lldfa/mid2.g").rule("E").
//                input("acedbx", "").check();

//        Builder.tree("lldfa/mid.g").rule("E")
//                //.input("cx", "E#1{A#2{'c'}, 'x'}")
//                .input("acbx", "E#1{A#1{'a', A#2{'c'}, 'b'}, 'x'}")
//                .input("aacbbx", "")
//                .input("aay", "E#2{['a', 'a'], 'y'}")
//                .check();
        Builder.tree("lldfa/mid.g").rule("E")
                .input("aadbcx", "E#1{A#1{'a', 'a', A#2{'d'}, 'b', 'c'}, 'x'}")
                .input("aaaadbcbcx", "")
                .check();
    }


    @Test
    public void greedy() throws Exception {
//        DescTester.check2(Builder.tree("lldfa/greedy-opt.g").rule("E").
//                input("ax", "").
//                input("aax", ""));
        Builder.tree("lldfa/greedy-opt2.g").rule("E").
                input("cya", "").
                input("cyaebda", "").
                check();

        single("lldfa/greedy-opt2.g");
    }

    @Test
    public void emitter() throws IOException {
        //Tree tree = Env.tree("lldfa/factor.g");
        var tree = Env.tree("rec/cyc.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LLDfaBuilder builder = new LLDfaBuilder(tree);
        builder.factor();

        var emitter = new GrammarEmitter(builder);
        emitter.emitFor("A");
        tree.file = new File(tree.file.getParent(), Utils.newName(tree.file.getName(), "-emit.g"));
        builder.tree = tree;
        //dot(builder);
    }

    @Test
    public void test_token_stream() throws Exception {
        var tree = Env.tree("lexer/skip.g");
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        LexerGenerator.gen(tree, Lang.JAVA);
        CcGenJava.writeTS(tree.options);
        var cl = new URLClassLoader(new URL[]{Env.dotDir().toURI().toURL()});
        var lexerCls = cl.loadClass("Lexer");
        var lexerCons = lexerCls.getDeclaredConstructor(Reader.class);
        var lexer = lexerCons.newInstance(new StringReader("abc"));

        var tsCls = cl.loadClass("TokenStream");
        var tsCons = tsCls.getDeclaredConstructor(lexerCls);
        var ts = tsCons.newInstance(lexer);
        var mark = tsCls.getDeclaredMethod("mark");
        var unmark = tsCls.getDeclaredMethod("unmark");
        var method = tsCls.getDeclaredMethod("consume", int.class, String.class);
        var res = method.invoke(ts, 0, "");
        cl.close();
    }

    @Test
    public void all0() throws Exception {
        //E: A x | A y
        //A: a b | a c
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
        Builder.tree("lldfa/action.g").rule("E")
                .dump()
                .input("ab", "")
                .check();
    }

    @Test
    public void astSimple() throws Exception {
//        Builder.tree("lldfa/ast.g").rule("A")
//                .dump()
//                .input("a", "A#1{'a'}")
//                .input("ab", "A#2{'a', 'b'}")
//                .input("abc", "A#3{'a', 'b', 'c'}")
//                .check();
        Builder.tree("lldfa/simple.g").rule("E").
                input("acx", "E#1{A#1{'a'}, B#1{'c'}, 'x'}").
                input("adx", "E#1{A#1{'a'}, B#2{'d'}, 'x'}").
                input("bcx", "E#1{A#2{'b'}, B#1{'c'}, 'x'}").
                input("bdx", "E#1{A#2{'b'}, B#2{'d'}, 'x'}").
                input("acy", "E#2{A#1{'a'}, D#1{'c'}, 'y'}").
                input("ady", "E#2{A#1{'a'}, D#2{'d'}, 'y'}").
                input("bcy", "E#2{A#2{'b'}, D#1{'c'}, 'y'}").
                input("bdy", "E#2{A#2{'b'}, D#2{'d'}, 'y'}")
                .check();
    }

    @Test
    public void sr() throws Exception {
        Builder.tree("lldfa/sr.g").rule("E")
                .input("abcdx", "E#1{A{'a'}, C#1{'b'}, 'c', 'd', 'x'}")
                .input("aecdx", "E#1{A{'a'}, C#2{'e'}, 'c', 'd', 'x'}")
                .input("abcdy", "E#2{'a', B{'b'}, 'c', 'd', 'y'}")
                .check();

        Builder.tree("lldfa/sr2.g").rule("E")
                .input("abcx", "E#1{A{'a', 'b'}, 'c', 'x'}")
                .input("abcy", "E#2{B{'a', 'b', 'c'}, 'y'}")
                .check();

        Builder.tree("lldfa/sr-loop.g").rule("E")
                .input("abcx", "E#1{[A{C{'a', 'b'}, 'c'}], 'x'}")
                .input("abcabcx", "E#1{[A{C{'a', 'b'}, 'c'}, A{C{'a', 'b'}, 'c'}], 'x'}")
                .input("abcy", "E#2{[B{'a', 'b', 'c'}], 'y'}")
                .input("abcabcy", "E#2{[B{'a', 'b', 'c'}, B{'a', 'b', 'c'}], 'y'}")
                .check();
    }

    @Test
    public void multi() throws Exception {
        Builder.tree("lldfa/multi-sym.g").rule("E")
                .input("ax", "E#1{'a', 'x'}")
                .input("ay", "E#2{'a', 'y'}")
                .input("zpt", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}]}")
                .input("zptc", "E#3{A{'z'}, [B#1{'p'}, B#2{'t'}], 'c'}")
                .rule("F")
                .input("abx", "F#1{'a', 'b', 'x'}")
                .input("bx", "F#1{'b', 'x'}")
                .input("by", "F#2{'b', 'y'}").check();
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
                .check();
        Builder.tree("lldfa/rr-loop.g").rule("F").
                input("x", "F#1{X{'x'}}").
                input("y", "F#2{Y{'y'}}").
                input("ax", "F#1{X{[A#1{'a'}], 'x'}}").
                input("ababx", "F#1{X{[A#1{'a'}, A#2{'b'}, A#1{'a'}, A#2{'b'}], 'x'}}").
                input("ay", "F#2{Y{[B#1{'a'}], 'y'}}").
                input("bbaay", "F#2{Y{[B#2{'b'}, B#2{'b'}, B#1{'a'}, B#1{'a'}], 'y'}}")
                .check();
        Builder.tree("lldfa/rr-loop2.g").rule("E").
                dump().
                input("acx", "E#1{[A#1{'a'}], [B#1{'c'}], 'x'}").
                input("bdx", "E#1{[A#2{'b'}], [B#2{'d'}], 'x'}").
                input("abcdx", "E#1{[A#1{'a'}, A#2{'b'}], [B#1{'c'}, B#2{'d'}], 'x'}").
                input("acy", "E#2{[C#1{'a'}], [D#1{'c'}], 'y'}").
                input("bdy", "E#2{[C#2{'b'}], [D#2{'d'}], 'y'}").
                input("abcdy", "E#2{[C#1{'a'}, C#2{'b'}], [D#1{'c'}, D#2{'d'}], 'y'}").
                check();
        Builder.tree("lldfa/rr-loop-len2.g").rule("E").
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
                .check();
        Builder.tree("lldfa/rr-loop2-len2.g").rule("E").
                input("abefx", "E#1{A#1{'a', 'b'}, B#1{'e', 'f'}, 'x'}").
                input("abcdefghx", "E#1{A#1{'a', 'b'}, [A#2{'c', 'd'}], B#1{'e', 'f'}, [B#2{'g', 'h'}], 'x'}").
                input("abefy", "E#2{C#1{'a', 'b'}, D#1{'e', 'f'}, 'y'}").
                input("abcdefghy", "E#2{C#1{'a', 'b'}, [C#2{'c', 'd'}], D#1{'e', 'f'}, [D#2{'g', 'h'}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-x.g").rule("E").
                input("x", "E#1{'x'}").
                input("y", "E#2{'y'}").
                input("abacx", "E#1{[A{'a', B#1{'b'}}, A{'a', B#2{'c'}}], 'x'}").
                input("abacy", "E#2{[C{'a', D#1{'b'}}, C{'a', D#2{'c'}}], 'y'}").
                check();
        Builder.tree("lldfa/rr-loop-deep.g").rule("E").
                input("x", "E#1{'x'}").
                input("acbdx", "E#1{[A#1{C#1{'a'}}, A#1{C#2{'c'}}, A#2{D#1{'b'}}, A#2{D#2{'d'}}], 'x'}").
                input("y", "E#2{'y'}").
                input("acbdy", "E#2{[B#1{K#1{'a'}}, B#1{K#2{'c'}}, B#2{M#1{'b'}}, B#2{M#2{'d'}}], 'y'}")
                .check();
        Builder.tree("lldfa/rr-loop-sub.g").rule("F").
                input("z", "F#2{'z'}").
                input("aaz", "F#2{['a', 'a'], 'z'}").
                input("x", "F#1{E#1{'x'}}").
                input("y", "F#1{E#2{'y'}}").
                input("ax", "F#1{E#1{[A#1{'a'}], 'x'}}").
                input("aby", "F#1{E#2{[B#1{'a'}, B#2{'b'}], 'y'}}")
                .check();
    }

    @Test
    public void name() throws Exception {
        Builder.tree("lldfa/rr-loop-sub.g").rule("F").
                input("z", "F#2{'z'}").
                input("aaz", "F#2{['a', 'a'], 'z'}").
                input("x", "F#1{E#1{'x'}}").
                input("y", "F#1{E#2{'y'}}").
                input("ax", "F#1{E#1{[A#1{'a'}], 'x'}}").
                input("aby", "F#1{E#2{[B#1{'a'}, B#2{'b'}], 'y'}}")
                .check();
    }

    @Test
    public void cc() throws Exception {
        var tree = Env.tree("lldfa/factor.g");
        // var tree = Env.tree("lldfa/rr-loop.g");
        tree.options.dump = true;
        //ParserGen.genCC(tree, Lang.JAVA);
        Builder.tree("lldfa/factor.g").rule("E")
                .input("c", "E#1{'c'}")
                .input("d", "E#2{'d'}")
                .input("aaad", "E#2{['a', 'a', 'a'], 'd'}")
                .input("aaac", "E#1{[A#1{'a'}, A#1{'a'}, A#1{'a'}], 'c'}")
                .input("bc", "E#1{[A#2{'b'}], 'c'}")
                .input("aaabc", "E#1{[A#1{'a'}, A#1{'a'}, A#1{'a'}, A#2{'b'}], 'c'}")
                .checkCC();
    }
}
