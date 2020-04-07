package rule;

import nodes.NodeList;
import nodes.Tree;

//list of rules
//rhs
public class Sequence extends Rule {

    NodeList<Rule> list = new NodeList<>();

    public Sequence(Rule...arr){
        for(Rule r:arr){
            list.add(r);
        }
    }
    
    public void add(Rule rule) {
        list.add(rule);
    }

    public Rule transform(RuleDecl decl, Tree tree) {
        Sequence s=new Sequence();
        for (Rule rule : list.list) {
            s.add(rule.transform(decl,tree));
        }
        return s;
    }

    public Rule normal() {
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
