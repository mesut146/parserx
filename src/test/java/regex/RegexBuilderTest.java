package regex;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.dfa.NfaReader;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Shortcut;
import mesut.parserx.nodes.TokenDecl;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.regex.RegexFromStr;
import mesut.parserx.regex.RegexOptimizer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;

public class RegexBuilderTest {

    @Test
    public void lineComment() throws IOException {
        NFA nfa = NfaReader.read("final=2\nstart=0\n0->1,/\n1->2,/\n2->2,[^\\n]");
        Node node = new RegexBuilder(nfa).buildRegex();
        System.out.println(node);
    }

    @Test
    public void blockComment() throws IOException {
        Node node = Shortcut.from("block_comment");
        Tree tree = new Tree();
        tree.addToken(new TokenDecl("comment", node));
        NFA nfa = NFABuilder.build(tree).dfa();
        Minimization.optimize(nfa);
        nfa.dot(new FileWriter(Env.dotFile("a.dfa")));
    }

    @Test
    @Ignore
    public void build() throws Exception {
        NFA nfa = NfaReader.read(Env.getResFile("fsm/comment.nfa"));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        //regexBuilder.setOrder(5, 4, 3, 2, 1);
        regexBuilder.setOrder(2, 3, 4, 1, 5);
        System.out.println(regexBuilder.buildRegex());
    }

    @Test
    public void fsm() throws IOException {
        NFA nfa = NfaReader.read(Env.getResFile("fsm/a.nfa"));
        //NFA dfa = nfa.dfa();
        //nfa.dfa().dot(new FileWriter(Env.dotFile("a.dot")));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        Node regex = regexBuilder.buildRegex();
        System.out.println(regex);
        System.out.println(new RegexOptimizer(regex).optimize());
    }

    @Test
    public void fromStr() {
        System.out.println(RegexFromStr.build("(asd?(ab)+)\\["));
        System.out.println(RegexFromStr.build("[a-z*+?()][^\r\n][[abc][a\\]]"));
    }
}
