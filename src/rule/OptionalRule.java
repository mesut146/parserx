package rule;

//rule?
public class OptionalRule extends Rule {
    //group or simple name
    public Rule rule;

    public OptionalRule() {
    }

    public void transform() {
        if (rule.isName()) {
            RuleDecl r = new RuleDecl(rule.toString() + "?");
            OrRule or = new OrRule();
            or.add(rule);
            or.add(new EmptyRule());
            r.rhs = or;
        }
    }

    public OptionalRule(Rule node) {
        this.rule = node;
    }

    @Override
    public String toString() {
        return rule + "?";
    }


}
