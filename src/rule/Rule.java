package rule;

import nodes.Node;
import nodes.*;

//base class for parser rules
//optional,star,plus,group
public class Rule extends Node {

    public RuleDecl decl;

    public boolean isName() {
        return this instanceof RuleRef;
    }

    public RuleRef asName() {
        return (RuleRef) this;
    }

    public boolean isStar() {
        return this instanceof StarRule;
    }

    public StarRule asStar() {
        return (StarRule) this;
    }

    public boolean isPlus() {
        return this instanceof PlusRule;
    }

    public PlusRule asPlus() {
        return (PlusRule) this;
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
    }

    /*public boolean isRegex() {
        return this instanceof RegexRule;
    }

    public RegexRule asRegex() {
        return (RegexRule) this;
    }*/

    public boolean isGroup() {
        return this instanceof GroupRule;
    }

    public GroupRule asGroup() {
        return (GroupRule) this;
    }

    public boolean isSequence() {
        return this instanceof Sequence;
    }

    public Sequence asSequence() {
        return (Sequence) this;
    }
    
    
    public Rule transform(RuleDecl decl,Tree tree){
        return this;
    }
}
