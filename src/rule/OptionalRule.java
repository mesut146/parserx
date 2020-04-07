package rule;
import nodes.*;

//rule?
public class OptionalRule extends Rule {
    //group or simple name
    public Rule rule;

    public OptionalRule() {
    }

    
    //r=a (s1 s2)? b
    //r=a r_g? b
    //r_g?=r_g;
    //r_g?=;
    //r_g=s1 s2;
    public Rule transform(RuleDecl decl,Tree tree) {
        RuleRef rl=rule.transform(decl,tree).asName();
        String nm=rl.name+"?";
        RuleDecl r1=new RuleDecl(nm);
        RuleDecl r2=new RuleDecl(nm);
        r1.rhs=rl;
        r2.rhs=new EmptyRule();
        tree.addRule(r1);
        tree.addRule(r2);
        
        return new RuleRef(nm);
    }

    public OptionalRule(Rule node) {
        this.rule = node;
    }

    @Override
    public String toString() {
        return rule + "?";
    }


}
