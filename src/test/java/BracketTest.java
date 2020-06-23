import dfa.CharClass;
import nodes.Bracket;
import nodes.RangeNode;

import java.util.Arrays;

public class BracketTest {

    static void bracketTest() {
        Bracket b = new Bracket();
        b.add(new RangeNode(10, 15));
        b.add(new RangeNode(20, 30));
        b.add(new RangeNode(25, 28));
        //b.add(new RangeNode(25, 50));
        b.add(new RangeNode(35, 50));
        System.out.println("negated=" + b.negateAll());
    }

    static void segmentTest() {
        int l = 'a';
        int r = 'z';
        int seg = CharClass.segment(l, r);
        System.out.println(Arrays.toString(CharClass.desegment(seg)));
    }

}
