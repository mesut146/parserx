package rule;
import nodes.*;

//rule+
public class PlusNode extends Node {

    public Node node;

    public PlusNode() {
    }

    public PlusNode(Rule node) {
        this.node = node;
    }
    
    //r=a e+ b;
    //e+=e;
    //e+=e+ e;
    /*public Rule transform(RuleDecl decl,Tree tree) {
        RuleRef rl=rule.transform(decl,tree).asName();
        String nm=rl.name+"+";
        RuleRef ref=new RuleRef(nm);

        RuleDecl r1=ref.declare();
        RuleDecl r2=ref.declare();
        r1.rhs=rl;
        r2.rhs=new Sequence(ref,rl);
        tree.addRule(r1);
        tree.addRule(r2);
        return ref;
    }*/

    @Override
    public String toString() {
        return node + "+";
    }


}
