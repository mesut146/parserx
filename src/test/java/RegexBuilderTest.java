import common.Env;
import lexer.RealTest;
import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.dfa.Validator;
import mesut.parserx.nodes.*;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.regex.RegexOptimizer;
import mesut.parserx.regex.RegexPrinter;
import mesut.parserx.regex.parser.RegexVisitor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import parser.DescTester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RegexBuilderTest {

    @Test
    public void buildTest() throws IOException {
        NFA nfa = NFA.read(Env.getResFile("fsm/line_comment.dfa"));
        Node node = new RegexBuilder(nfa).buildRegex();
        System.out.println(node);
        Assert.assertEquals("\"/\" \"/\" [^\\u0000-\\t\\u000b-\\uffff]*",node.toString());
    }

    @Test
    public void buildTest2() throws Exception {
        NFA nfa = NFA.read(Env.getResFile("fsm/comment.dfa"));
        RegexBuilder builder = new RegexBuilder(nfa);
        //builder.setOrder(5, 4, 3, 2, 1);
        builder.setOrder(2, 3, 4, 1, 5);
        var regex = builder.buildRegex();
        System.out.println(regex);
        Assert.assertEquals("\"/\" \"*\" [^\\u0000-\\u0029\\u002b-\\uffff]* \"*\" ([^\\u0000-\\u002e\\u0030-\\uffff] [^\\u0000-\\u0029\\u002b-\\uffff]* \"*\")* \"/\"", regex.toString());
    }

    @Test
    public void buildTest3() throws IOException {
        NFA nfa = NFA.read(Env.getResFile("fsm/a.nfa"));
        Node regex = RegexBuilder.from(nfa);
        System.out.println(regex);
    }

    @Test
    public void buildTest4() throws IOException {
        NFA nfa = NFA.read(Env.getResFile("fsm/string.dfa"));
        Node node = new RegexBuilder(nfa).buildRegex();
        System.out.println(RegexPrinter.print(node));
        Assert.assertEquals("\"([^\"\\]|\\.)*\"",RegexPrinter.print(node));
    }

    void testRegex(String regex) throws IOException {
        Assert.assertEquals(regex,RegexPrinter.print(RegexVisitor.make(regex)));
    }

    @Test
    public void strToRegex() throws IOException {
        testRegex("(asd?(ab)+)\\[");
        testRegex("[a-z*+?()]");
        testRegex("[^\\r\\n]");
        testRegex("[[abc]");
        testRegex("[a\\]]");
    }

}
