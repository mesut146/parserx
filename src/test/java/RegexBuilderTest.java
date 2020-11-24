import dfa.NFA;
import dfa.RegexBuilder;
import nodes.*;
import org.junit.Test;

import java.io.File;

public class RegexBuilderTest {

    //  /* (^*)* * (^/ (^*)* *)* /


    @Test
    public void fromGrammar() throws Exception {
        File file = Env.getResFile("javaLexer.g");
        NFA nfa = NfaTest.makeNFA(file);
        //nfa.dump(null);
        Node node = new RegexBuilder(nfa).buildRegex();
        Transformer transformer = new Transformer() {
            @Override
            public Node transformOr(OrNode node) {
                OrNode newNode = new OrNode();
                Bracket bracket = new Bracket();
                for (Node ch : node) {
                    ch = transform(ch);
                    if (ch.isString() && ch.asString().value.length() == 1) {
                        bracket.add(new Bracket.CharNode(ch.asString().value.charAt(0)));
                    }
                    else if (ch.isRange()) {
                        RangeNode rangeNode = ch.asRange();
                        if (rangeNode.isSingle()) {
                            bracket.add(new Bracket.CharNode((char) rangeNode.start));
                        }
                        else {
                            bracket.add(ch);
                        }
                    }
                    else {
                        newNode.add(ch);
                    }
                }
                bracket.normalize();
                newNode.add(bracket);
                return newNode;
            }
        };
        node = transformer.transform(node);
        System.out.println(node);
    }

    @Test
    public void build() throws Exception {
        StringNode.string_quote = true;
        NFA nfa = NfaReader.read(Env.getResFile("fsm/string.nfa"));
        System.out.println(new RegexBuilder(nfa).buildRegex());
    }
}
