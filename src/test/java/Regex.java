import nodes.Bracket;
import nodes.Node;
import nodes.RangeNode;
import nodes.StringNode;
import org.junit.Test;
import regex.RegexOptimizer;
import regex.RegexUtils;

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
