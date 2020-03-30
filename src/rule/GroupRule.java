package rule;

import nodes.Tree;

//(rule1 rule2)
public class GroupRule extends Rule {

    public Rule rhs;//sequence,or

    public Rule transform(RuleDecl decl, Tree tree) {
        //r = pre (e1 e2) end;
        //r = pre rg end;
        //r_g = e1 e2;
        String nname = decl.name + "_g";
        RuleDecl d = new RuleDecl(nname);
        d.rhs = rhs;
        tree.addRule(d);
        return new RuleRef(nname);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

}
