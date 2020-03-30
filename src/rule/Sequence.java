package rule;

import nodes.NodeList;
import nodes.Tree;

//list of rules
//rhs
public class Sequence extends Rule {

    NodeList<Rule> list = new NodeList<>();

    public void add(Rule rule) {
        list.add(rule);
    }

    public void transform(RuleDecl decl, Tree tree) {
        for (Rule rule : list.list) {

        }
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
