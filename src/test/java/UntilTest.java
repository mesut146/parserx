import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import org.junit.Test;

public class UntilTest {
    @Test
    public void name() throws Exception {
        Tree tree = Tree.makeTree(Env.getResFile("until.g"));
        NFA nfa = tree.makeNFA();
        Node r = RegexBuilder.from(nfa);
        System.out.println(r);
    }
}
