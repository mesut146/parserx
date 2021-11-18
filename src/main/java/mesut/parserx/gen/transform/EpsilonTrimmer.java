package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;


//make grammar epsilon free
public class EpsilonTrimmer extends Transformer {
    public static boolean preserveNames = false;
    boolean modified;
    Tree out;

    public EpsilonTrimmer(Tree tree) {
        super(tree);
        out = new Tree(tree);
    }

    public static Tree trim(Tree input) {
        Simplify.all(input);
        EpsilonTrimmer trimmer = new EpsilonTrimmer(input);
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
            new Transformer(out) {
                @Override
                public Node visitName(Name name, Void parent) {
                    if (name.isRule()) {
                        return new Name(trimSuffix(name.name));
                    }
                    return name;
                }
            }.transformRules();
        }
        return out;
    }

    boolean canBeEmpty(Node node) {
        return Helper.canBeEmpty(node, tree);
    }

    boolean isEmpty(Node node) {
        return FirstSet.tokens(node, tree).isEmpty();
    }

    boolean hasEpsilon(Node node) {
        final boolean[] has = {false};
        new Transformer(tree) {
            @Override
            public Node visitName(Name name, Void parent) {
                if (Helper.canBeEmpty(name, tree)) {
                    has[0] = true;
                }
                return super.visitName(name, parent);
            }

            @Override
            public Node visitEpsilon(Epsilon node, Void parent) {
                has[0] = true;
                return node;
            }

            @Override
            public Node visitRegex(Regex regex, Void parent) {
                if (regex.isOptional() || regex.isStar()) {
                    has[0] = true;
                    return regex;
                }
                return super.visitRegex(regex, parent);
            }
        }.transformNode(node, null);
        return has[0];
    }

    @Override
    public Node visitGroup(Group node, Void arg) {
        node.node = transformNode(node.node, arg);
        return node;
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            Node ch = seq.get(i);
            if (canBeEmpty(ch)) {//!ch.isName()
                List<Node> l1 = new ArrayList<>(seq.list);
                l1.remove(i);
                if (isEmpty(ch)) {
                    return new Sequence(l1);
                }
                List<Node> l2 = new ArrayList<>(l1);
                l2.add(i, transformNode(ch, arg));
                Or res = new Or();
                res.add(new Sequence(l1));
                res.add(new Sequence(l2));
                //modified = true;
                if (hasEpsilon(res)) {
                    return transformNode(res, arg);
                }
                return res;
            }
            else if (hasEpsilon(ch)) {
                seq.set(i, transformNode(ch, arg));
            }
        }
        return seq;
    }

    @Override
    public Node visitName(Name name, Void parent) {
        if (Helper.canBeEmpty(name, tree)) {
            return new Name(name.name + "_noe");
        }
        return name;
    }

    @Override
    public Node visitRegex(Regex regex, Void parent) {
        Node ch = transformNode(regex.node, parent);
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
    public Node visitOr(Or or, Void arg) {
        Or res = new Or();
        for (Node ch : or) {
            if (ch.isEpsilon()) continue;
            if (hasEpsilon(ch)) {
                ch = transformNode(ch, arg);
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
    public Node visitEpsilon(Epsilon node, Void parent) {
        return node;
    }

}
