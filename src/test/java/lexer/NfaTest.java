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
import mesut.parserx.utils.Log;
import mesut.parserx.utils.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import parser.Builder;

import java.io.*;
import java.util.logging.Level;

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
    public void lineComment() throws IOException {
        NFA nfa = NFA.read("start=0\nfinal=2\n0->1,/\n1->2,/\n2->2,[^\\n]");
        Node node = new RegexBuilder(nfa).buildRegex();
        //Assert.assertEquals("\"/\" \"/\" [^\\n]*", node.toString());
        Assert.assertEquals("\"/\" \"/\" [^\\u0000-\\t\\u000b-\\uffff]*", node.toString());
    }

    @Test
    public void dot() throws IOException {
        Tree tree = Env.tree("lexer/skip.g");
        var nfa = tree.makeNFA().dfa();
        var dot = Env.dotFile("skip");
        nfa.dot(dot);
        Utils.runDot(dot);
    }


    @Test
    @Ignore
    public void hopcroft() throws Exception {
        NFA dfa = NFA.read(Env.getResFile("fsm/dfa2.dfa"));
        //Minimization.removeUnreachable(dfa);
        //dfa = Minimization.optimize(dfa);
        dfa = Minimization.Hopcroft(dfa);
        dfa.dump();
        dfa.dot(Env.dotFile("dfa1"));
    }

    @Test
    @Ignore
    public void minimizeMy() throws Exception {
        NFA dfa = NFA.read(Env.getResFile("fsm/min.dfa"));
        dfa.dump();
        Minimization.optimize(dfa).dump();
    }

    @Test
    public void loop() throws Exception {
        Tree tree = Env.tree("fsm/loop.g");
        NFA dfa = tree.makeNFA().dfa();
        //dfa = Minimization.Hopcroft(dfa);
        dfa = Minimization.optimize(dfa);
        dfa.dot(Env.dotFile("dfa1"));
        dfa.dump();
    }
}
