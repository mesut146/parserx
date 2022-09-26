package lexer;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.nodes.*;
import mesut.parserx.parser.AstVisitor;
import mesut.parserx.parser.Lexer;
import mesut.parserx.parser.Parser;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class NfaTest {

    static Node makeRegex(String regex) throws IOException {
        return new AstVisitor().visitRhs(new Parser(new Lexer(new StringReader(regex))).rhs());
    }

    @Test
    public void splitRanges() throws IOException {
        Tree tree = new Tree();
        var block = new TokenBlock();
        tree.tokenBlocks.add(block);
        tree.addToken(new TokenDecl("hex", makeRegex("[a-f]")), block);
        tree.addToken(new TokenDecl("rest", makeRegex("[b-z]")), block);
        tree.makeNFA().dump(new PrintWriter(System.out));
    }

    void check(NFA nfa, int state, String input, int... target) {
        var st = nfa.getState(state);
        var in = nfa.getAlphabet().getId(new StringNode(input));
        var targets = nfa.getTargets(st, in);
        for (var trg : target) {
            Assert.assertTrue(targets.contains(nfa.getState(trg)));
        }
    }

    @Test
    public void reader() throws Exception {
        NFA nfa = NFA.read(Env.getResFile("fsm/in.nfa"));
        check(nfa, 0, "a", 0, 1);
        check(nfa, 0, "b", 0);
        check(nfa, 1, "b", 2);
    }

    @Test
    public void reader2() throws IOException {
        Tree tree = Tree.makeTree(new File("./src/main/grammar/nfaReader.g"));
        tree.options.outDir = Env.dotDir().getAbsolutePath();
        ParserGen.gen(tree, Lang.JAVA);
    }

    @Test
    public void lineComment() throws IOException {
        NFA nfa = NFA.read("start=0\nfinal=2\n0->1,/\n1->2,/\n2->2,[^\\n]");
        Node node = new RegexBuilder(nfa).buildRegex();
        Assert.assertEquals("\"/\" \"/\" [^\\n]*", node.toString());
    }

    @Test
    public void dot() throws IOException {
        Tree tree = Env.tree("lexer/skip.g");
        var nfa = tree.makeNFA().dfa();
        var dot = Env.dotFile("skip");
        nfa.dot(new FileWriter(dot));
        Env.dot(dot);
    }

    @Test
    public void mode() throws IOException {
        //Tree tree = Env.tree("lexer/mode.g");
        var tree = Tree.makeTree(new File("doc/xml.g"));
        var nfa = tree.makeNFA();
        //nfa.dump();
        System.out.println("----------------");
        var dfa = nfa.dfa();
        dfa.dump();
    }

    @Test
    public void hopcroft() throws Exception {
        NFA dfa = NFA.read(Env.getResFile("min/dfa2.dfa"));
        //Minimization.removeUnreachable(dfa);
        //dfa = Minimization.optimize(dfa);
        dfa = Minimization.Hopcroft(dfa);
        dfa.dump();
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
    }

    @Test
    public void minimizeMy() throws Exception {
        //File file = Env.getResFile("fsm/dfa2.dfa");
        NFA dfa = NFA.read(Env.getResFile("min/dfa-min.dfa"));
        Minimization.removeUnreachable(dfa);
        Minimization.removeDead(dfa);
        Minimization.optimize(dfa);
        //dfa.dump(new PrintWriter(System.out));
    }

    @Test
    public void loop() throws Exception {
        Tree tree = Env.tree("min/a.g");
        NFA dfa = tree.makeNFA().dfa();
        //dfa = Minimization.Hopcroft(dfa);
        dfa = Minimization.optimize(dfa);
        dfa.dot(new FileWriter(Env.dotFile("dfa1")));
        dfa.dump();
    }

    @Test
    public void difference() throws IOException {
        var tree = Env.tree("lexer/sub.g");
        var nfa = tree.makeNFA();
        nfa.dump();
    }
}
