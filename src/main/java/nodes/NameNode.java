package nodes;

import rule.Rule;
import rule.RuleDecl;

//right side
//can refer to rule or token
public class NameNode extends Rule {

    public String name;
    public boolean isToken;//if we reference to a token

    public NameNode(String name) {
        this.name = name;
    }

    public RuleDecl declare() {
        return new RuleDecl(name);
    }

    @Override
    public String toString() {
        if (isToken) {
            return "{" + name + "}";
        }
        return name;
    }


}
