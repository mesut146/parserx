package rule;

import nodes.NodeList;
import java.util.*;
import nodes.*;

// rule1 | rule2
public class OrRule extends Rule{
    
    //seq,single
    public NodeList<Rule> list=new NodeList<>();
    
    public void add(Rule rule){
        list.add(rule);
    }

    @Override
    public Rule transform(RuleDecl decl, Tree tree)
    {
        //*r=a (e1|e2) b;
        //r=a r_g b;
        //r_g=e1|e2|e3;
        //r_g=e1;
        //r_g=e2;
        for(Rule rule:list.list){
            RuleDecl rd=new RuleDecl(decl.name);
            rd.rhs=rule.transform(rd,tree);
            tree.addRule(rd);
        }
        return null;
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
        //sb.append("(");
        sb.append(list.join(" | "));
        //sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return normal();
    }


}
