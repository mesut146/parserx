import mesut.parserx.nodes.Bracket;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.RangeNode;
import mesut.parserx.nodes.StringNode;
import org.junit.Test;
import mesut.parserx.regex.RegexOptimizer;
import mesut.parserx.regex.RegexUtils;

public class Regex {
    @Test
    public void negate() throws Exception {
        Node regex = new StringNode("abcd");
        Node negated = RegexUtils.negate(regex);
        negated = new RegexOptimizer(negated).optimize();
        System.out.println(negated);
    }

    @Test
    public void bracketTest() {
        Bracket b = new Bracket();
        b.add(new RangeNode(10, 15));
        b.add(new RangeNode(20, 30));
        b.add(new RangeNode(25, 28));
        //b.add(new RangeNode(25, 50));
        b.add(new RangeNode(35, 50));
        System.out.println("negated=" + b.negateAll());
    }
}
