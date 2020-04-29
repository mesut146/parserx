package nodes;

import nodes.*;

//can be lexer group or parser group
//(rule1 rule2)
public class GroupNode<T extends Node> extends Node {

    //public Rule rhs;//sequence,or,single
    public T rhs;
    
    static int count=0;

    /*public Rule transform(RuleDecl decl, Tree tree) {
        //r = pre (e1 e2) end;
        //r = pre r_g end;
        //r_g = e1 e2;
        Rule r=(Rule)rhs;
        String nname = decl.name + "_g"+(count++);
        RuleDecl d = new RuleDecl(nname);
        if(r.isOr()){
            r.transform(d,tree);
        }else{
            d.rhs = r.transform(decl,tree);
            tree.addRule(d);
        }
        return new RuleRef(nname);
    }*/
    
    //simplify (e) -> e
    /*public Rule normal(){
        Rule r=(Rule)rhs;
        if(!r.isOr()&&!r.isSequence()){
            return r;
        }
        return (Rule)this;
    }*/

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(rhs);
        sb.append(")");
        return sb.toString();
    }

}
