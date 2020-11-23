import dfa.Alphabet;
import dfa.NFA;
import dfa.RegexBuilder;
import grammar.ParseException;
import nodes.Node;
import nodes.RangeNode;
import nodes.StringNode;
import nodes.Tree;
import org.junit.Test;

public class RegexBuilderTest {

    //  /* (^*)* * (^/ (^*)* *)* /

    @Test
    public void build() throws ParseException {
        StringNode.string_quote = true;
        Node starNot = new StringNode("*'");
        Node divNot = new StringNode("/'");

        Alphabet alphabet = new Alphabet();
        alphabet.add(RangeNode.of('/'));
        alphabet.add(RangeNode.of('*'));
        alphabet.addRegex(starNot);//not star
        alphabet.addRegex(divNot);//not div

        Tree tree = new Tree();
        tree.alphabet = alphabet;

        NFA nfa = new NFA(100);
        nfa.tree = tree;
        nfa.setAccepting(4, true);
        nfa.addTransition(0, 1, alphabet.getId('/'));
        nfa.addTransition(1, 2, alphabet.getId('*'));
        nfa.addTransition(2, 2, alphabet.getId(starNot));
        nfa.addTransition(2, 3, alphabet.getId('*'));
        nfa.addTransition(3, 2, alphabet.getId(divNot));
        nfa.addTransition(3, 4, alphabet.getId('/'));
        //nfa.dump(null);

        System.out.println(new RegexBuilder(nfa).buildRegex());
    }
}
