package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;


//convert epsilons to '?'
//or eliminate them by expanding
public class EpsilonTrimmer extends Transformer {
    public static boolean remove = false;
    Tree tree, res;
    List<String> hasEpsilon = new ArrayList<>();

    public EpsilonTrimmer(Tree tree) {
        this.tree = tree;
        res = new Tree(tree);
    }

    public static Tree trim(Tree input) {
        EpsilonTrimmer trimmer = new EpsilonTrimmer(input);
        return trimmer.trim();
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
        if (e == null) {
            return super.transformOr(node);
        }
        if (remove) {
            return e;
        }
        else {
            return new RegexNode(new GroupNode(e).normal(), "?");
        }
    }

    /*@Override
    public Node transformName(NameNode node) {
        if (node.isRule()) {
            Node rhs = tree.getRule(node.name);
            if () {

            }
            Node n = Helper.hasEps();
        }
        return super.transformName(node);
    }*/
}
