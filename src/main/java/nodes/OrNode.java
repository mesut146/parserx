package nodes;

import nodes.NodeList;

import java.util.*;

import nodes.*;

// rule1 | rule2 | rule3...
public class OrNode extends Node implements Iterable<Node> {

    //seq,single
    public NodeList<Node> list = new NodeList<>();

    public void add(Node rule) {
        list.add(rule);
    }

    /*
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
    }*/

    /*String array() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(list.join(""));
        sb.append("]");
        return sb.toString();
    }*/

    //print with bars
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


    @Override
    public Iterator<Node> iterator() {
        return list.iterator();
    }
}
