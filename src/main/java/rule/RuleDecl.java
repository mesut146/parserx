package rule;

import nodes.Node;

//rule decl in grammar
//name=rules;
public class RuleDecl extends Node {

    public String name;
    public Node rhs;//sequence,or rule

    public RuleDecl() {
    }

    public RuleDecl(String name) {
        this.name = name;
    }

    public RuleDecl(String name, Node rhs) {
        this.name = name;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return name + " = " + rhs + ";";
    }


}
