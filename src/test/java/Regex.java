import nodes.Node;
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
}
