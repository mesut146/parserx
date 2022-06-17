package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;

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

    //trim major epsilon so that result is at least one token long
    //E: ... -> E1: ...,     E: E1 | €
    public Info trim(Node node) {
        if (!Helper.canBeEmpty(node, tree)) {
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
        Info res = new Info();
        boolean empty = Helper.canBeEmpty(regex.node, tree);
        if (regex.isOptional()) {
            if (empty) {
                //A? = A | € = A_no_eps | A_eps | €
                Info tmp = trim(regex.node);
                res.eps = tmp.eps;
                res.noEps = tmp.noEps;
            }
            else {
                res.eps = new Epsilon();
                res.noEps = regex.node;
            }
            return res;
        }
        else if (regex.isStar()) {
            if (empty) {
                //A* = A+ | € = A A* | €
                //(A_eps | A_noe) A* | €
                //A_eps A* | A_noe A*
                Info tmp = trim(regex.node);
                Node no = tmp.noEps;
                no.astInfo.isInLoop = true;
                res.noEps = new Sequence(no, regex.copy());
                res.eps = tmp.eps;
            }
            else {
                //A+ | €
                res.noEps = new Regex(regex.node, RegexType.PLUS);
                res.noEps.astInfo = regex.astInfo.copy();
                res.eps = new Epsilon();
            }
            return res;
        }
        else {
            //node must be empty
            //A+ = A A* = (A_noe | A_eps) A*
            //A_noe A* | A_eps A*
            Info tmp = trim(regex.node);
            Regex star = new Regex(regex.node, RegexType.STAR);
            star.astInfo = regex.astInfo.copy();
            Node no = tmp.noEps.copy();
            no.astInfo.isInLoop = true;
            res.noEps = new Sequence(no, star);
            res.eps = tmp.eps.copy();
            return res;
        }
    }

    public Info visitOr(Or or, Void arg) {
        Info res = new Info();
        Node a = or.first();
        Node b = Helper.trim(or);
        if (Helper.canBeEmpty(a, tree)) {
            Info a1 = trim(a);
            if (Helper.canBeEmpty(b, tree)) {
                //A | B = A1 | € | B1 | € = A1 | B1 | €
                Info b1 = trim(b);
                res.noEps = Or.make(a1.noEps, b1.noEps);
                res.eps = orEps(a1.eps, b1.eps);
            }
            else {
                //A | B = A1 | € | B
                if (a1.noEps == null) {
                    res.noEps = b;
                }
                else {
                    res.noEps = Or.make(a1.noEps, b);
                }
                res.eps = a1.eps;
            }
        }
        else {
            //b must be empty
            // A | B = A | B1 | €
            Info tmp = trim(b);
            res.noEps = tmp.noEps == null ? a : new Or(a, tmp.noEps);
            res.eps = tmp.eps;
        }
        return res;
    }

    @Override
    public Info visitSequence(Sequence s, Void arg) {
        Info res = new Info();
        Info a = trim(s.first());
        Info b = trim(Helper.trim(s));
        //both a b are empty
        //A B = (A1 | A0) (B1 | B0) = A1 B1 | A1 A0 | A0 B1 | A0 B0;
        Node s1 = (a.noEps == null || b.noEps == null) ? null : Sequence.make(a.noEps, b.noEps);
        Node s2 = a.noEps == null ? null : Sequence.make(a.noEps, b.eps);
        Node s3 = b.noEps == null ? null : Sequence.make(a.eps, b.noEps);
        Node s4 = Sequence.make(a.eps, b.eps);
        s4.astInfo = s.astInfo.copy();
        List<Node> or = new ArrayList<>();
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
        Factor.check(s1);
        Factor.check(s2);
        Factor.check(s3);
        Factor.check(s4);
        return res;
    }

    public Info visitName(Name name, Void arg) {
        Info res = new Info();
        if (name.astInfo.isFactored) {
            res.eps = name;
            return res;
        }
        if (isFactored(name)) {
            //still factored but chained
            res.eps = name;
            return res;
        }


        Name noName = tree.getNoEps(name);
        Name epsName = tree.getEps(name);

        RuleDecl decl = tree.getRule(name);
        Name epsRef = Factor.inherit(epsName, decl);
        Name noRef = Factor.inherit(noName, decl);

        Info tmp = null;
        if (tree.getRule(noName) == null) {
            tmp = trim(decl.rhs);
            RuleDecl noDecl = new RuleDecl(noRef, tmp.noEps);
            noDecl.retType = decl.retType;
            tree.addRuleBelow(noDecl, decl);
            res.noEps = noName;
        }
        else {
            res.noEps = noName;
        }
        if (tree.getRule(epsName) == null) {
            if (tmp == null) {
                tmp = trim(decl.rhs);
            }
            if (!tmp.eps.isEpsilon()) {
                //factored eps still needs ast builder
                RuleDecl epsDecl = new RuleDecl(epsRef, tmp.eps);
                epsDecl.retType = decl.retType;
                tree.addRuleBelow(epsDecl, decl);
                res.eps = epsName;
            }
        }
        else {
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
