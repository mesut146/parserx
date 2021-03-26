package gen;

import nodes.*;


//convert epsilons to '?'
public class EpsilonTrimmer extends Transformer {
    Tree tree, res;

    public EpsilonTrimmer(Tree tree) {
        this.tree = tree;
        res = new Tree(tree);
    }

    public Tree trim() {
        for (RuleDecl rule : tree.rules) {
            rule = transformRule(rule);
            if (rule != null) {
                res.addRule(rule);
            }
        }
        return res;
    }

    @Override
    public Node transformOr(OrNode node) {
        Node e = Helper.hasEps(node);
        if (e != null) {
            return new RegexNode(new GroupNode(e).normal(), "?");
        }
        return super.transformOr(node);
    }

}
