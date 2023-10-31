package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//make grammar epsilon free
public class EpsilonTrimmer extends Transformer {
    public static String suffix = "_noe";
    Set<Name> emptyRules = new HashSet<>();
    boolean modified;
    List<RuleDecl> newRules = new ArrayList<>();

    public EpsilonTrimmer(Tree tree) {
        super(tree);
    }

    public static void trim(Tree input) {
        Simplify.all(input);
        var trimmer = new EpsilonTrimmer(input);
        trimmer.trim();
    }

    public void trim() {
        for (var rd : tree.rules) {
            if (FirstSet.canBeEmpty(rd.rhs, tree)) {
                emptyRules.add(rd.ref);
            }
        }
        for (int i = 0; i < tree.rules.size(); i++) {
            var rule = tree.rules.get(i);

            var rhs = transformNode(rule.rhs, null);
            String name = rule.ref.name;
            if (emptyRules.contains(rule.ref)) {
                name = name + suffix;
            }
            var res = new RuleDecl(name, rhs);
            if (!rhs.isEpsilon()) {
                res.rhs = Simplify.simplifyNode(rhs);
                newRules.add(res);
            }
        }
        tree.rules = newRules;
        if (tree.start != null && emptyRules.contains(tree.start)) {
            tree.start = new Name(tree.start.name + suffix);
        }
    }


    boolean hasEpsilon(Node node) {
        var tr = new BaseVisitor<Void, Void>() {
            boolean has = false;

            @Override
            public Void visitName(Name name, Void parent) {
                if (emptyRules.contains(new Name(trimSuffix(name.name)))) {
                    has = false;
                    return null;
                }
                has = FirstSet.canBeEmpty(name, tree);
                return null;
            }

            @Override
            public Void visitEpsilon(Epsilon node, Void parent) {
                has = true;
                return null;
            }

            @Override
            public Void visitRegex(Regex regex, Void parent) {
                if (regex.isOptional() || regex.isStar()) {
                    has = true;
                    return null;
                }
                return regex.node.accept(this, null);
            }
        };
        node.accept(tr, null);
        return tr.has;
    }

    @Override
    public Node visitGroup(Group node, Void arg) {
        node.node = transformNode(node.node, arg);
        return node;
    }

    String trimSuffix(String str) {
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    boolean canBeEmpty(Node node) {
        if (!node.isName()) return FirstSet.canBeEmpty(node, tree);
        var name = node.asName();
        if (name.name.endsWith(suffix) && emptyRules.contains(new Name(trimSuffix(name.name)))) {
            return false;
        }
        return FirstSet.canBeEmpty(node, tree);
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            var ch = seq.get(i);
            if (canBeEmpty(ch)) {//!ch.isName()
                //a b? c=a b c | a c
                List<Node> l1 = new ArrayList<>(seq.list);
                l1.remove(i);
                l1.add(i, transformNode(ch, arg));
                List<Node> l2 = new ArrayList<>(seq.list);
                l2.remove(i);
                if (FirstSet.isEmpty(ch, tree)) {
                    return Sequence.make(l2);
                }
                Or res = new Or(Sequence.make(l1), Sequence.make(l2));
                //modified = true;
                return res.accept(this, null);
            } else {
                seq.set(i, transformNode(ch, arg));
            }
        }
        return seq;
    }

    @Override
    public Node visitName(Name name, Void parent) {
        if (emptyRules.contains(name)) {
            return new Name(name.name + suffix);
        }
        return new Name(name.name, name.isToken);
    }

    @Override
    public Node visitRegex(Regex regex, Void parent) {
        var ch = transformNode(regex.node, parent);
        if (regex.isOptional()) {
            //a?=a|€
            modified = true;
            return ch;
        } else if (regex.isStar()) {
            //a*=a+|€
            modified = true;
            return new Regex(ch, RegexType.PLUS);
        } else {
            return new Regex(ch, RegexType.PLUS);
        }
    }

    @Override
    public Node visitOr(Or or, Void arg) {
        List<Node> list = new ArrayList<>();
        for (var ch : or) {
            if (ch.isEpsilon()) continue;
            ch = transformNode(ch, arg);
            if (!ch.isEpsilon()) {
                list.add(ch);
            }
        }
        if (list.isEmpty()) return new Epsilon();
        return Or.make(list);
    }

    @Override
    public Node visitEpsilon(Epsilon node, Void parent) {
        return node;
    }

}
