package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;


//make grammar epsilon free
public class EpsilonTrimmer2 extends SimpleTransformer {
    public static boolean preserveNames = false;
    boolean modified;
    Tree out;

    public EpsilonTrimmer2(Tree tree) {
        super(tree);
        out = new Tree(tree);
    }

    public static Tree trim(Tree input) {
        Simplify.all(input);
        EpsilonTrimmer2 trimmer = new EpsilonTrimmer2(input);
        return trimmer.trim();
    }

    static String trimSuffix(String s) {
        if (s.endsWith("_noe")) {
            return s.substring(0, s.length() - 4);
        }
        return s;
    }

    public Tree trim() {
        for (int i = 0; i < tree.rules.size(); i++) {
            RuleDecl rule = tree.rules.get(i);
            Node rhs = transformNode(rule.rhs, null);
            RuleDecl res = new RuleDecl(rule.ref.name + "_noe", rhs);
            if (rhs.isEpsilon()) {
                res.hidden = true;
            }
            else {
                res.rhs = Simplify.simplifyNode(rhs);
            }
            out.addRule(res);
        }
        if (preserveNames) {
            for (RuleDecl decl : out.rules) {
                decl.ref.name = trimSuffix(decl.ref.name);
            }
            new SimpleTransformer(out) {
                @Override
                public Node transformName(Name node, Node parent) {
                    if (node.isRule()) {
                        return new Name(trimSuffix(node.name));
                    }
                    return node;
                }
            }.transformRules();
        }
        return out;
    }

    boolean canBeEmpty(Node node) {
        return Helper.canBeEmpty(node, tree);
    }

    boolean isEmpty(Node node) {
        return Helper.first(node, tree, true, false, true).isEmpty();
    }

    boolean hasEpsilon(Node node) {
        final boolean[] has = {false};
        new SimpleTransformer(tree) {
            @Override
            public Node transformName(Name node, Node parent) {
                if (Helper.canBeEmpty(node, tree)) {
                    has[0] = true;
                }
                return super.transformName(node, parent);
            }

            @Override
            public Node transformEpsilon(Epsilon node, Node parent) {
                has[0] = true;
                return node;
            }

            @Override
            public Node transformRegex(Regex regex, Node parent) {
                if (regex.isOptional() || regex.isStar()) {
                    has[0] = true;
                    return regex;
                }
                return super.transformRegex(regex, parent);
            }
        }.transformNode(node, null);
        return has[0];
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
                List<Node> l1 = new ArrayList<>(seq.list);
                l1.remove(i);
                if (isEmpty(ch)) {
                    return new Sequence(l1);
                }
                List<Node> l2 = new ArrayList<>(l1);
                l2.add(i, transformNode(ch, seq));
                Or res = new Or();
                res.add(new Sequence(l1));
                res.add(new Sequence(l2));
                //modified = true;
                if (hasEpsilon(res)) {
                    return transformNode(res, parent);
                }
                return res;
            }
            else if (hasEpsilon(ch)) {
                seq.set(i, transformNode(ch, seq));
            }
        }
        return seq;
    }

    @Override
    public Node transformName(Name node, Node parent) {
        if (Helper.canBeEmpty(node, tree)) {
            Name res = new Name(node.name + "_noe");
            return res;
        }
        return node;
    }

    @Override
    public Node transformRegex(Regex regex, Node parent) {
        Node ch = transformNode(regex.node, regex);
        if (regex.isOptional()) {
            //a?=a|€
            modified = true;
            return ch;
        }
        else if (regex.isStar()) {
            modified = true;
            return new Regex(ch, "+");
        }
        else {
            return new Regex(ch, "+");
        }
    }

    @Override
    public Node transformOr(Or or, Node parent) {
        Or res = new Or();
        for (Node ch : or) {
            if (ch.isEpsilon()) continue;
            if (hasEpsilon(ch)) {
                ch = transformNode(ch, or);
            }
            if (!ch.isEpsilon()) {
                res.add(ch);
            }
        }
        if (res.size() == 0) return new Epsilon();
        if (res.size() == 1) return res.first();
        return res;
    }

    @Override
    public Node transformEpsilon(Epsilon node, Node parent) {
        return node;
    }

}
