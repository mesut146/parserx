package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


//make grammar epsilon free
public class EpsilonTrimmer2 extends SimpleTransformer {
    boolean modified;
    HashSet<Name> emptyRules = new HashSet<>();

    public EpsilonTrimmer2(Tree tree) {
        super(tree);
    }

    public static Tree trim(Tree input) {
        Simplify.all(input);
        EpsilonTrimmer2 trimmer = new EpsilonTrimmer2(input);
        return trimmer.trim();
    }

    public Tree trim() {
        for (int i = 0; i < tree.rules.size(); i++) {
            //System.out.println(i + "/" + tree.rules.size());
            RuleDecl rule = tree.rules.get(i);
            modified = false;
            if (canBeEmpty(rule.rhs)) {
                //rule.hidden = true;
                emptyRules.add(rule.ref());
            }
            transformRule(rule);
            if (rule.rhs == null || rule.rhs.isEpsilon()) {
                tree.rules.remove(i);
            }
            else {
                rule.rhs = Simplify.simplifyNode(rule.rhs);
            }
            if (modified) {
                modified = false;
                i--;
            }
        }
        return tree;
    }

    boolean canBeEmpty(Node node) {
        if (node.isName()) {
            if (emptyRules.contains(node.asName())) {
                return true;
            }
        }
        return Helper.canBeEmpty(node, tree);
    }

    @Override
    public Node transformGroup(Group node, Node parent) {
        node.node = transformNode(node.node, node);
        return node;
    }

    @Override
    public Node transformSequence(Sequence seq, Node parent) {
        for (int i = 0; i < seq.size(); i++) {
            Node ch = seq.get(i);
            if (canBeEmpty(ch)) {//!ch.isName()
                Or res = new Or();
                ch = transformNode(ch, seq);
                List<Node> l1 = new ArrayList<>(seq.list);
                l1.remove(i);
                List<Node> l2 = new ArrayList<>(l1);
                l2.add(i, ch);
                res.add(new Sequence(l1));
                res.add(new Sequence(l2));
                //modified = true;
                return res;
            }
        }
        return seq;
    }

    @Override
    public Node transformRegex(Regex regex, Node parent) {
        Node ch = transformNode(regex.node, regex);
        if (regex.isOptional()) {
            //a?=a|€
            if (!parent.isSequence()) {
                //throw new RuntimeException();
            }
            modified = true;
            return ch;
        }
        else if (regex.isStar()) {
            if (!parent.isSequence()) {
                //throw new RuntimeException();
            }
            modified = true;
            return new Regex(ch, "+");
        }
        return regex;
    }

    @Override
    public Node transformEpsilon(Epsilon node, Node parent) {
        return null;
    }

}
