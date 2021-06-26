package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;


//convert epsilons to '?'
//or eliminate them by expanding
public class EpsilonTrimmer extends SimpleTransformer {
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

    //A B C = A B C
    Or replace(Sequence node, int i, Node opt) {
        //A B? C = A B C | A C
        List<Node> l1 = new ArrayList<>(node.list);
        List<Node> l2 = new ArrayList<>(node.list);
        l1.remove(i);
        l2.remove(i);
        l1.add(i, opt);
        return new Or(new Sequence(l1).normal(), new Sequence(l2)).dups();
    }

    @Override
    public Node transformSequence(Sequence node, Node parent) {
        for (int i = 0; i < node.size(); i++) {
            Node ch = node.get(i);
            if (ch.isRegex()) {
                Regex regex = ch.asRegex();
                if (regex.isOptional()) {
                    //A B? C = A B C | A C
                    return transformOr(replace(node, i, regex.node), parent);
                }
                else if (regex.isStar()) {
                    //A B* C = A B+ C | A C
                    return transformOr(replace(node, i, new Regex(regex.node, "+")), parent);
                }
            }
            else if (ch.isName()) {
                Name name = ch.asName();
                if (name.isRule()) {
                    RuleDecl decl = tree.getRule(name.name);
                    if (decl.rhs.isOptional()) {
                        //substitute
                        return transformOr(replace(node, i, decl.rhs.asRegex().node), parent);
                    }
                    else if (decl.rhs.isStar()) {
                        //substitute
                        return transformOr(replace(node, i, new Regex(decl.rhs.asRegex().node, "+")), parent);
                    }
                }
            }
        }
        return node;
    }

    @Override
    public Node transformOr(Or node, Node parent) {
        Node tmp = super.transformOr(node, parent);
        if (tmp.isOr()) {
            node = tmp.asOr();
        }
        else {
            return tmp;
        }
        for (Node ch : node) {
            if (ch.isRegex()) {
                Regex regex = ch.asRegex();
                if (regex.isStar()) {

                }
                else if (regex.isOptional()) {

                }
            }
        }
        return node;
    }

    @Override
    public Node transformRegex(Regex node, Node parent) {
        if (node.isOptional()) {
            if (!parent.isSequence() && !parent.isOr() && !(parent instanceof RuleDecl)) {
                throw new RuntimeException("invalid parent of optional: " + parent);
            }
        }
        return super.transformRegex(node, parent);
    }
}
