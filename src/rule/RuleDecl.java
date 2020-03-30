package rule;

import nodes.Node;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public String name;
    public Rule rhs;//sequence,or rule

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " = " + rhs + ";";
    }


}
