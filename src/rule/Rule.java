package rule;

import nodes.Node;
import nodes.*;

//base class for parser rules
//optional,star,plus,group,or
public class Rule extends Node {

    public RuleDecl decl;

    public boolean isName() {
        return this instanceof RuleRef;
    }

    public RuleRef asName() {
        return (RuleRef) this;
    }

    /*public boolean isStar() {
        return this instanceof StarRule;
    }

    public StarRule asStar() {
        return (StarRule) this;
    }*/

    /*public boolean isPlus() {
        return this instanceof PlusNode;
    }

    public PlusNode asPlus() {
        return (PlusNode) this;
    }

    public boolean isOptional() {
        return this instanceof OptionalRule;
    }

    public OptionalRule asOptional() {
        return (OptionalRule) this;
    }
    
    public boolean isOr() {
        return this instanceof OrRule;
    }

    public OrRule asOr() {
        return (OrRule) this;
    }*/

    /*public boolean isRegex() {
        return this instanceof RegexNode;
    }

    public RegexNode asRegex() {
        return (RegexNode) this;
    }

    public boolean isGroup() {
        return this instanceof GroupNode;
    }

    public GroupNode asGroup() {
        return (GroupNode) this;
    }

    public boolean isSequence() {
        return this instanceof Sequence;
    }

    public Sequence asSequence() {
        return (Sequence) this;
    }*/
    
    
    public Rule transform(RuleDecl decl,Tree tree){
        return this;
    }
}
