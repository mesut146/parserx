package rule;

import nodes.NodeList;

import java.util.ArrayList;
import java.util.List;

//(rule1 rule2)
public class GroupRule extends Rule {

    public Rule rhs;//sequence,or


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

}
