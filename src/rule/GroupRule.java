package rule;

import nodes.Tree;

//(rule1 rule2)
public class GroupRule extends Rule {

    public Rule rhs;//sequence,or

    public void transform(RuleDecl decl, Tree tree) {
        //r = pre (e1 e2) end;
        //r = pre rg end;
        //rg = e1 e2;
        RuleDecl d = new RuleDecl();

        decl.rhs = rhs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

}
