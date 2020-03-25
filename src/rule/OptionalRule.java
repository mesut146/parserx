package rule;

//rule?
public class OptionalRule extends Rule {
    //group or simple name
    public Rule rule;

    public OptionalRule() {
    }

    public OptionalRule(Rule node) {
        this.rule = node;
    }

    @Override
    public String toString() {
        return rule + "?";
    }


}
