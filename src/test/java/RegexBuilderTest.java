import mesut.parserx.dfa.NFA;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.StringNode;
import org.junit.Ignore;
import org.junit.Test;
import mesut.parserx.utils.NfaReader;
import mesut.parserx.regex.RegexOptimizer;

import java.io.File;

public class RegexBuilderTest {

    //  /* (^*)* * (^/ (^*)* *)* /

    @Test
    @Ignore
    public void fromGrammar() throws Exception {
        File file = Env.getResFile("javaLexer.g");
        NFA nfa = NfaTest.makeNFA(file);
        //nfa.dump(null);
        Node node = new RegexBuilder(nfa).buildRegex();
        node = new RegexOptimizer(node).optimize();
        System.out.println(node);
    }

    @Test
    @Ignore
    public void build() throws Exception {
        StringNode.string_quote = false;
        NFA nfa = NfaReader.read(Env.getResFile("fsm/comment.nfa"));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        //regexBuilder.setOrder(5,4,3,2,1);
        //regexBuilder.setOrder(2, 3, 4, 1, 5);
        System.out.println(regexBuilder.buildRegex());
    }
}
