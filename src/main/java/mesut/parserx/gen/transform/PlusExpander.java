package mesut.parserx.gen.transform;

import mesut.parserx.nodes.*;

import java.util.*;

public class PlusExpander extends Transformer {

    Set<Name> done = new HashSet<>();
    Map<RuleDecl, RuleDecl> ruleMap = new HashMap<>();//new rule->below
    public boolean left;

    public PlusExpander(Tree tree, boolean left) {
        super(tree);
        this.left = left;
    }

    @Override
    public void transformRules() {
        super.transformRules();
        for (var e : ruleMap.entrySet()) {
            tree.addRuleBelow(e.getKey(), e.getValue());
        }
    }

    @Override
    public Node visitRegex(Regex regex, Void arg) {
        if (regex.isPlus()) {
            var sym = regex.node.asName();
            if (!done.contains(sym)) {
                var ref = new Name(sym.name + "+");
                RuleDecl rd;
                if (left) {
                    //a+: a+ a | a;
                    rd = new RuleDecl(ref, new Or(new Sequence(ref, sym), new Sequence(sym)));
                }
                else {
                    //a+: a a+ | a;
                    rd = new RuleDecl(ref, new Or(new Sequence(sym, ref), new Sequence(sym)));
                }
                ruleMap.put(rd, curRule);
                return ref;
            }
        }
        return regex;
    }
}
