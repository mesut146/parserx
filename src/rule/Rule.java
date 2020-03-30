package rule;

import nodes.Node;

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

    public boolean isRegex() {
        return this instanceof RegexRule;
    }

    public RegexRule asRegex() {
        return (RegexRule) this;
    }

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
}
