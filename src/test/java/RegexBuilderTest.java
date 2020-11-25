import dfa.NFA;
import dfa.RegexBuilder;
import nodes.*;
import org.junit.Test;
import utils.NfaReader;
import utils.RegexOptimizer;

import java.io.File;

public class RegexBuilderTest {

    //  /* (^*)* * (^/ (^*)* *)* /


    @Test
    public void fromGrammar() throws Exception {
        File file = Env.getResFile("javaLexer.g");
        NFA nfa = NfaTest.makeNFA(file);
        //nfa.dump(null);
        Node node = new RegexBuilder(nfa).buildRegex();
        node = new RegexOptimizer(node).optimize();
        System.out.println(node);
    }

    @Test
    public void build() throws Exception {
        StringNode.string_quote = false;
        NFA nfa = NfaReader.read(Env.getResFile("fsm/tilde.nfa"));
        RegexBuilder regexBuilder = new RegexBuilder(nfa);
        //regexBuilder.setOrder(5,4,3,2,1);
        //regexBuilder.setOrder(2, 3, 4, 1, 5);
        System.out.println(regexBuilder.buildRegex());
    }
}
