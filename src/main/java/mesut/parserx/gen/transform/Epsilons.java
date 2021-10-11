package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

public class Epsilons {
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
            return null;
        }
        Info res = new Info();
        res.eps = new Epsilon();
        if (node.isRegex()) {
            Regex regex = node.asRegex();
            return trimRegex(res, regex);
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            return trimOr(res, or);
        }
        else if (node.isSequence()) {
            Sequence s = node.asSequence();
            return trimSeq(res, s);
        }
        else if (node.isGroup()) {
            Info tmp = trim(node.asGroup().node);
            res.noEps = new Group(tmp.noEps);
            res.noEps.astInfo = node.astInfo;
            res.eps = tmp.eps;
            return res;
        }
        else if (node.isName()) {
            Name name = node.asName();
            return trimName(res, name);
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }

    private Info trimRegex(Info res, Regex regex) {
        boolean empty = Helper.canBeEmpty(regex.node, tree);
        if (regex.isOptional()) {
            if (empty) {
                //A? = A | € = A_no_eps | €
                res = trim(regex.node);
            }
            else {
                res.noEps = regex.node;
            }
            return res;
        }
        else if (regex.isStar()) {
            if (empty) {
                //A* = A+ | € = A A* | € = (A1 | €) A1* | € = A1 A1* | A1* | €
                //A1 A1* | A1+ | € = A1+ | €
                Info tmp = trim(regex.node);
                res.noEps = new Regex(tmp.noEps, "+");
                res.eps = tmp.eps;
            }
            else {
                //A+ | €
                res.noEps = new Regex(regex.node, "+");
            }
            return res;
        }
        else {
            //plus
            //node must be empty
            //A+ = A A* = (A_no_eps | €) (A_no_eps | €)* =  A_no_eps A_no_eps* | A_no_eps*
            //A_no_eps (A_no_eps)* | A_no_eps+ | € = A_no_eps+ | €
            Info tmp = trim(regex.node);
            res.noEps = new Regex(tmp.noEps, "+");
            res.eps = tmp.eps;
            return res;
        }
    }

    private Info trimOr(Info res, Or or) {
        Node a = or.first();
        Node b = Helper.trim(or);
        if (Helper.canBeEmpty(a, tree)) {
            Info a1 = trim(a);
            if (Helper.canBeEmpty(b, tree)) {
                //A | B = A1 | € | B1 | € = A1 | B1 | €
                Info b1 = trim(b);
                res.noEps = new Or(a1.noEps, b1.noEps);
                res.eps = orEps(a1.eps, b1.eps);
            }
            else {
                //A | B = A1 | € | B
                if (a1.noEps == null) {
                    res.noEps = b;
                }
                else {
                    res.noEps = new Or(a1.noEps, b);
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

    private Info trimSeq(Info res, Sequence s) {
        Info a = trim(s.first());
        Info b = trim(Helper.trim(s));
        //both a b are empty
        //A B = (A1 | A0) (B1 | B0) = A1 B1 | A1 A0 | A0 B1 | A0 B0;
        Node s1 = a.noEps == null ? null : new Sequence(a.noEps, b.noEps);
        Node s2 = a.noEps == null ? null : new Sequence(a.noEps, b.eps).normal();
        Node s3 = b.noEps == null ? null : new Sequence(a.eps, b.noEps).normal();
        Node s4 = new Sequence(a.eps, b.eps).normal();
        s4.astInfo.code = s.astInfo.code;
        Or or = new Or();
        if (s1 != null) {
            s1.astInfo.code = s.astInfo.code;
            or.add(s1);
        }
        if (s2 != null) {
            s2.astInfo.code = s.astInfo.code;
            or.add(s2);
        }
        if (s3 != null) {
            s3.astInfo.code = s.astInfo.code;
            or.add(s3);
        }
        res.noEps = or.normal();
        res.eps = s4;
        Factor.check(s1);
        Factor.check(s2);
        Factor.check(s3);
        Factor.check(s4);
        return res;
    }

    private Info trimName(Info res, Name name) {
        if (name.astInfo.isFactored) {
            res.eps = name;
            return res;
        }
        if (isFactored(name)) {
            //still factored but chained
            res.eps = name;
            return res;
        }

        RuleDecl decl = tree.getRule(name);

        Name noName = new Name(name.name + "_noe");
        noName.args = name.args;
        noName.astInfo = name.astInfo.copy();
        RuleDecl noDecl = tree.getRule(noName);

        Name epsName = new Name(name.name + "_eps");
        epsName.args = name.args;
        epsName.astInfo = name.astInfo.copy();
        RuleDecl epsDecl = tree.getRule(epsName);

        Info tmp = null;
        if (noDecl == null) {
            tmp = trim(decl.rhs);
            noDecl = new RuleDecl(noName, tmp.noEps);
            noDecl.retType = decl.retType;
            tree.addRule(noDecl);
            res.noEps = noName;
        }
        else {
            res.noEps = noName;
        }
        if (epsDecl == null) {
            if (tmp == null) {
                tmp = trim(decl.rhs);
            }
            if (!tmp.eps.isEpsilon()) {
                //factored eps still needs ast builder
                epsDecl = new RuleDecl(epsName, tmp.eps);
                epsDecl.retType = decl.retType;
                tree.addRule(epsDecl);
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
        return Helper.first(a, tree, true, false, true).isEmpty();
    }

    public static class Info {
        Node noEps;
        Node eps;
    }
}
