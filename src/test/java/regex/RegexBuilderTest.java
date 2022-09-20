package regex;

import common.Env;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.nodes.*;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.regex.RegexOptimizer;
import mesut.parserx.regex.RegexPrinter;
import mesut.parserx.regex.parser.RegexVisitor;
import org.junit.Ignore;
import org.junit.Test;
import parser.DescTester;

import java.io.FileWriter;
import java.io.IOException;

public class RegexBuilderTest {

    @Test
    public void lineComment() throws IOException {
        NFA nfa = NFA.read("start=0\nfinal=2\n0->1,/\n1->2,/\n2->2,[^\\n]");
        Node node = new RegexBuilder(nfa).buildRegex();
        System.out.println(node);
    }

    @Test
    public void blockComment() throws IOException {
        Node node = Shortcut.from("block_comment");
        Tree tree = new Tree();
        var block = new TokenBlock();
        tree.tokenBlocks.add(block);
        tree.addToken(new TokenDecl("comment", node), block);
        NFA nfa = NFABuilder.build(tree).dfa();
        Minimization.optimize(nfa);
        nfa.dot(new FileWriter(Env.dotFile("a.dfa")));
    }

    @Test
    @Ignore
    public void build() throws Exception {
        NFA nfa = NFA.read(Env.getResFile("fsm/comment.nfa"));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        //regexBuilder.setOrder(5, 4, 3, 2, 1);
        regexBuilder.setOrder(2, 3, 4, 1, 5);
        System.out.println(regexBuilder.buildRegex());
    }

    @Test
    public void fsm() throws IOException {
        NFA nfa = NFA.read(Env.getResFile("fsm/a.nfa"));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        Node regex = regexBuilder.buildRegex();
        System.out.println(regex);
        System.out.println(RegexPrinter.print(regex));
        System.out.println(new RegexOptimizer(regex).optimize());
    }

    @Test
    public void fromStr() throws IOException {
        System.out.println(RegexVisitor.make("(asd?(ab)+)\\["));
        System.out.println(RegexVisitor.make("[a-z*+?()]"));
        System.out.println(RegexVisitor.make("[^\r\n]"));
        System.out.println(RegexVisitor.make("[[abc]"));
        System.out.println(RegexVisitor.make("[a\\]]"));
    }

}
