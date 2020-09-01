package rule;

import nodes.Node;

//for lr parsers,in place of optional node
public class EmptyRule extends Node {

    @Override
    public String toString() {
        return "";
    }

}
