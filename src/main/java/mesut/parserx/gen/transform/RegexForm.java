package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

//print rule as regex by substituting other refs
public class RegexForm extends Transformer {

    public RegexForm(Tree tree) {
        super(tree);
    }

    public static RuleDecl normalizeRule(RuleDecl decl, Tree tree) {
        decl.rhs = decl.rhs.accept(new RegexForm(tree), null);
        return decl;
    }

    public Node normalizeRule(RuleDecl decl) {
        return decl.rhs.copy().accept(this, null);
    }

    @Override
    public Node visitName(Name name, Void arg) {
        if (name.isRule()) {
            return tree.getRule(name).rhs.accept(this, arg);
        }
        return name;
    }
}
