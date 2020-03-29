package rule;

import nodes.NodeList;
import java.util.*;

// rule1 | rule2
public class OrRule extends Rule{
    
    //seq,single
    public NodeList<Rule> list=new NodeList<>();
    
    public void add(Rule rule){
        list.add(rule);
    }

    /*String array() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(list.join(""));
        sb.append("]");
        return sb.toString();
    }*/

    String normal() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(list.join("|"));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return normal();
    }


}
