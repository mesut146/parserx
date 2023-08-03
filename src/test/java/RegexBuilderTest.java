import common.Env;
import lexer.RealTest;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.nodes.*;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.regex.RegexOptimizer;
import mesut.parserx.regex.RegexPrinter;
import mesut.parserx.regex.parser.RegexVisitor;
import org.junit.Assert;
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
        Assert.assertEquals("\"/\" \"/\" [^\\u0000-\\t\\u000b-\\uffff]*",node.toString());
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
        RegexBuilder builder = new RegexBuilder(nfa);
        //builder.setOrder(5, 4, 3, 2, 1);
        builder.setOrder(2, 3, 4, 1, 5);
        var regex = builder.buildRegex();
        System.out.println(regex);
        Assert.assertEquals("\"/\" \"*\" [^\\u0000-\\u0029\\u002b-\\uffff]* \"*\" ([^\\u0000-\\u002e\\u0030-\\uffff] [^\\u0000-\\u0029\\u002b-\\uffff]* \"*\")* \"/\"", regex.toString());
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

    @Test
    public void regex() throws Exception {
        RealTest.check(Env.tree("lexer/reg.g"), "[^a-Za-zA-Z0-9]", "[_\n[\\]^-]", "[\\u0000\\u0011-\\u0022]", "[ç]");
    }
}
