package nodes;

import java.util.Iterator;
import java.util.List;

//list of rules
//rhs
public class Sequence extends NodeList {

    public Sequence(Node... arr) {
        super(arr);
    }

    public Sequence(List<Node> arr) {
        super(arr);
    }

    public static Node of(Node... a) {
        return new Sequence(a);
    }

    @Override
    public String toString() {
        return NodeList.join(list, " ");
    }

}
