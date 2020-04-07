package rule;

import nodes.Tree;

//(rule1 rule2)
public class GroupRule extends Rule {

    public Rule rhs;//sequence,or,single
    
    static int count=0;

    public Rule transform(RuleDecl decl, Tree tree) {
        //r = pre (e1 e2) end;
        //r = pre r_g end;
        //r_g = e1 e2;
        String nname = decl.name + "_g"+(count++);
        RuleDecl d = new RuleDecl(nname);
        if(rhs.isOr()){
            rhs.transform(d,tree);
        }else{
            d.rhs = rhs.transform(decl,tree);
            tree.addRule(d);
        }
        return new RuleRef(nname);
    }
    
    //simplify (e) -> e
    public Rule normal(){
        if(!rhs.isOr()&&!rhs.isSequence()){
            return rhs;
        }
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

}
