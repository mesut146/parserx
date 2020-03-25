package rule;
import nodes.*;
import java.util.*;

//(rule1 rule2)
public class GroupRule extends Rule
{
    List<Rule> list;
    
    public void add(Rule rule){
        list.add(rule);
    }
    
    
    public Rule getAs(int index)
    {
        return list.get(index);
    }

    
    
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        sb.append(NodeList.join(list," "));
        sb.append(")");
        return sb.toString();
    }
    
}
