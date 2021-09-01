package mesut.parserx.nodes;

import java.util.ArrayList;
import java.util.List;

public class Until extends Node {
    public Node node;
    public List<Bracket> brackets = new ArrayList<>();

    public Until(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "~" + node;
    }

}
