package rule;

import nodes.NodeList;
import nodes.Tree;
import nodes.*;

//list of rules
//rhs
public class Sequence extends Node {

    public NodeList<Node> list = new NodeList<>();

    public Sequence(Node...arr){
        for(Node r:arr){
            list.add(r);
        }
    }
    
    public void add(Node rule) {
        list.add(rule);
    }

    /*public Rule transform(RuleDecl decl, Tree tree) {
        Sequence s=new Sequence();
        for (Rule rule : list.list) {
            s.add(rule.transform(decl,tree));
        }
        return s;
    }*/

    public Node normal() {
        if (list.list.size() == 1) {
            return list.get(0);
        }
        return this;
    }

    @Override
    public String toString() {
        return list.join(" ");
    }


}
