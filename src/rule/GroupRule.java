package rule;

import nodes.NodeList;

import java.util.ArrayList;
import java.util.List;

//(rule1 rule2)
public class GroupRule extends Rule {

    List<Rule> list = new ArrayList<>();

    public void add(Rule rule) {
        list.add(rule);
    }
    
    public void addAll(List<Rule> other){
        list.addAll(other);
    }


    public Rule getAs(int index) {
        return list.get(index);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(NodeList.join(list, " "));
        sb.append(")");
        return sb.toString();
    }

}
