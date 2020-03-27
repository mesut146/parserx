package rule;

import nodes.NodeList;

// rule1 | rule2
public class OrRule extends NodeList {


    String array() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(join(""));
        sb.append("]");
        return sb.toString();
    }

    String normal() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(join("|"));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return normal();
    }


}
