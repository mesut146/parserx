package lexer;

import common.Env;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.StringNode;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.parser.AstBuilder;
import mesut.parserx.parser.Lexer;
import mesut.parserx.parser.Parser;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

public class NfaTest {

    static Node makeRegex(String regex) throws IOException {
        return new AstBuilder().visitRhs(new Parser(new Lexer(new StringReader(regex))).rhs());
    }

    @Test
    public void splitRanges() throws IOException {
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("hex", makeRegex("[a-f]")));
        tree.addToken(new TokenDecl("rest", makeRegex("[b-z]")));
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
        RecDescent.gen(tree, "java");
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
}
