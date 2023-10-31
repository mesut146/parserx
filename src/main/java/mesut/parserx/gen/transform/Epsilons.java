package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;

public class Epsilons extends BaseVisitor<Epsilons.Info, Void> {
    Tree tree;

    public Epsilons(Tree tree) {
        this.tree = tree;
    }

    public static Node trim(Node node, Tree tree) {
        return new Epsilons(tree).trim(node).noEps;
    }

    public static Info trimInfo(Node node, Tree tree) {
        return new Epsilons(tree).trim(node);
    }

    //copy decl args with names
    public static Name inherit(Name name, RuleDecl decl) {
        Name ref = name.copy();
        ref.args2.clear();
        ref.args2.addAll(decl.ref.args2);
        return ref;
    }

    //trim major epsilon so that result is at least one token long
    //E: ... -> E1: ...,     E: E1 | €
    public Info trim(Node node) {
        if (!FirstSet.canBeEmpty(node, tree)) {
            throw new RuntimeException("invalid call");
        }
        return node.accept(this, null);
    }

    @Override
    public Info visitGroup(Group group, Void arg) {
        Info res = new Info();
        Info tmp = trim(group.node);
        res.eps = new Group(tmp.eps);
        res.eps.astInfo = group.astInfo.copy();
        if (tmp.noEps != null) {
            res.noEps = new Group(tmp.noEps);
            res.noEps.astInfo = group.astInfo;
        }
        return res;
    }

    @Override
    public Info visitEpsilon(Epsilon epsilon, Void arg) {
        Info res = new Info();
        res.eps = new Epsilon();
        return res;
    }

    public Info visitRegex(Regex regex, Void arg) {
        var res = new Info();
        boolean empty = FirstSet.canBeEmpty(regex.node, tree);
        if (regex.isOptional()) {
            if (empty) {
                //A? = A | € = A_no_eps | A_eps | €
                var tmp = trim(regex.node);
                res.eps = tmp.eps;
                res.noEps = tmp.noEps;
            } else {
                res.eps = new Epsilon();
                res.noEps = regex.node;
            }
            return res;
        } else if (regex.isStar()) {
            if (empty) {
                //A* = A+ | € = A A* | €
                //(A_eps | A_noe) A* | €
                //A_eps A* | A_noe A*
                var tmp = trim(regex.node);
                var no = tmp.noEps;
                no.astInfo.isInLoop = true;
                res.noEps = new Sequence(no, regex.copy());
                res.eps = tmp.eps;
            } else {
                //A+ | €
                res.noEps = new Regex(regex.node, RegexType.PLUS);
                res.noEps.astInfo = regex.astInfo.copy();
                res.eps = new Epsilon();
            }
            return res;
        } else {
            //node must be empty
            //A+ = A A* = (A_noe | A_eps) A*
            //A_noe A* | A_eps A*
            var tmp = trim(regex.node);
            var star = new Regex(regex.node, RegexType.STAR);
            star.astInfo = regex.astInfo.copy();
            var no = tmp.noEps.copy();
            no.astInfo.isInLoop = true;
            res.noEps = new Sequence(no, star);
            res.eps = tmp.eps.copy();
            return res;
        }
    }

    public Info visitOr(Or or, Void arg) {
        var res = new Info();
        var a = or.first();
        var b = Helper.trim(or);
        if (FirstSet.canBeEmpty(a, tree)) {
            var a1 = trim(a);
            if (FirstSet.canBeEmpty(b, tree)) {
                //A | B = A1 | € | B1 | € = A1 | B1 | €
                var b1 = trim(b);
                res.noEps = Or.make(a1.noEps, b1.noEps);
                res.eps = orEps(a1.eps, b1.eps);
            } else {
                //A | B = A1 | € | B
                if (a1.noEps == null) {
                    res.noEps = b;
                } else {
                    res.noEps = Or.make(a1.noEps, b);
                }
                res.eps = a1.eps;
            }
        } else {
            //b must be empty
            // A | B = A | B1 | €
            var tmp = trim(b);
            res.noEps = tmp.noEps == null ? a : new Or(a, tmp.noEps);
            res.eps = tmp.eps;
        }
        return res;
    }

    @Override
    public Info visitSequence(Sequence s, Void arg) {
        var res = new Info();
        var a = trim(s.first());
        var b = trim(Helper.trim(s));
        //both a b are empty
        //A B = (A1 | A0) (B1 | B0) = A1 B1 | A1 A0 | A0 B1 | A0 B0;
        var s1 = (a.noEps == null || b.noEps == null) ? null : Sequence.make(a.noEps, b.noEps);
        var s2 = a.noEps == null ? null : Sequence.make(a.noEps, b.eps);
        var s3 = b.noEps == null ? null : Sequence.make(a.eps, b.noEps);
        var s4 = Sequence.make(a.eps, b.eps);
        s4.astInfo = s.astInfo.copy();
        var or = new ArrayList<Node>();
        if (s1 != null) {
            s1.astInfo = s.astInfo.copy();
            or.add(s1);
        }
        if (s2 != null) {
            s2.astInfo = s.astInfo.copy();
            or.add(s2);
        }
        if (s3 != null) {
            s3.astInfo = s.astInfo.copy();
            or.add(s3);
        }
        if (!or.isEmpty()) {
            res.noEps = Or.make(or);
        }
        res.eps = s4;
        return res;
    }

    @Override
    public Info visitFactored(Factored factored, Void arg) {
        var res = new Info();
        res.eps = factored;
        return res;
    }

    public Info visitName(Name name, Void arg) {
        var res = new Info();
        if (isFactored(name)) {
            //still factored but chained
            res.eps = name;
            return res;
        }

        var noName = tree.getNoEps(name);
        var epsName = tree.getEps(name);

        var decl = tree.getRule(name);
        var epsRef = inherit(epsName, decl);
        var noRef = inherit(noName, decl);

        Info tmp = null;
        if (tree.getRule(noName) == null) {
            tmp = trim(decl.rhs);
            var noDecl = new RuleDecl(noRef, tmp.noEps);
            noDecl.retType = decl.retType;
            tree.addRuleBelow(noDecl, decl);
        }
        res.noEps = noName;
        if (tree.getRule(epsName) == null) {
            if (tmp == null) {
                tmp = trim(decl.rhs);
            }
            if (!tmp.eps.isEpsilon()) {
                //factored eps still needs ast builder
                var epsDecl = new RuleDecl(epsRef, tmp.eps);
                epsDecl.retType = decl.retType;
                tree.addRuleBelow(epsDecl, decl);
                res.eps = epsName;
            }
        } else {
            res.eps = epsName;
        }
        return res;
    }

    private Node orEps(Node e1, Node e2) {
        if (e1.isEpsilon()) {
            return e2;
        }
        return e1;
    }

    private boolean isFactored(Node a) {
        return FirstSet.tokens(a, tree).isEmpty();
    }

    public static class Info {
        Node noEps;
        Node eps;
    }
}
