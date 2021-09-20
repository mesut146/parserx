package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class FactorLoop {
    public static boolean keepFactor = true;
    Tree tree;

    public FactorLoop(Tree tree) {
        this.tree = tree;
    }

    Factor.PullInfo pull(Node node, Name sym) {
        if (!Helper.first(node, tree, true).contains(sym)) {
            throw new RuntimeException("can't pull " + sym + " from " + node);
        }
        Factor.PullInfo info = new Factor.PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.equals(sym)) {
                if (keepFactor) {
                    name = name.copy();
                    name.astInfo.isFactored = true;
                    name.astInfo.factorName = sym.astInfo.factorName;
                    //todo
                    info.one = name;
                }
                else {
                    info.one = new Epsilon();
                }
            }
            else if (name.isRule()) {
                info = pullRule(name, sym);
            }
            else {
                info = null;
            }
        }
        else if (node.isGroup()) {
            info = pull(node.asGroup().node, sym);
        }
        else if (node.isOr()) {
            info = pullOr(node.asOr(), sym);
        }
        else if (node.isSequence()) {
            info = pullSeq(node.asSequence(), sym);
        }
        else if (node.isRegex()) {
            info = pullRegex(node.asRegex(), sym);
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass());
        }
        return info;
    }

    Factor.PullInfo pullOr(Or or, Name sym) {
        List<Node> one = new ArrayList<>();
        List<Node> zero = new ArrayList<>();
        for (Node ch : or) {
            Factor.PullInfo info = pull(ch, sym);
            if (info.one != null) {
                one.add(info.one);
                if (info.zero != null) {
                    zero.add(info.zero);
                }
            }
        }
        if (one.isEmpty()) {
            return null;
        }
        Factor.PullInfo res = new Factor.PullInfo();
        res.one = new Or(one).normal();
        if (!zero.isEmpty()) {
            res.zero = new Or(zero).normal();
        }
        return res;
    }


    Factor.PullInfo pullSeq(Sequence seq, Name sym) {
        Node a = seq.first();
        Node b = Helper.trim(seq);
        Factor.PullInfo info = pull(a, sym);
        if (info == null) {
            if (Helper.canBeEmpty(a, tree)) {
                //todo a can be empty b has loop
                info = pull(b, sym);
                if (info != null) {
                    //A_noe B | B = A_noe B | a* B_no_a B | B_no_a
                    Factor.PullInfo res = new Factor.PullInfo();
                    res.zero = new Sequence(Epsilons.trim(a, tree), b);
                    //res.one=
                    return res;
                }
            }
            //or multiple factor
            return null;
        }
        else {
            Factor.PullInfo res = new Factor.PullInfo();
            res.one = new Sequence(info.one, b).normal();
            if (info.zero != null) {
                res.zero = new Sequence(info.zero, b).normal();
            }
            return res;
        }
    }

    Factor.PullInfo pullRule(Name name, Name sym) {
        RuleDecl decl = tree.getRule(name);
        return pull(decl.rhs, sym);
    }

    Factor.PullInfo pullRegex(Regex regex, Name sym) {
        if (regex.isStar()) {
            if (regex.node.equals(sym)) {
                //base case
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Epsilon();
                return res;
            }
            Factor.PullInfo tmp = new Factor(tree).pull(regex.node, sym);
            if (tmp.one == null) {
                return null;
            }
            if (tmp.zero == null) {
                //A must start multiple a
                throw new RuntimeException("not supported yet");
            }
            //A* = (a A(a) | A_no_a)*
            if (isEpsilon(tmp.one)) {
                //(a A(a))* A_no_a A*
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Sequence(tmp.zero, regex.copy());
                return res;
            }
            if (Helper.canBeEmpty(tmp.one, tree)) {
                //A* = (a A_a_noe(a) | a | A_no_a)*
                //A* = a* (a A_a_noe(a) | A_no_a) A*
                //todo factor greedy
                Node trimmed = new Epsilons(tree).trim(tmp.one);
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Sequence(new Group(new Or(new Sequence(sym, trimmed), tmp.zero)), regex.copy());
                return res;
            }
        }
        else if (regex.isPlus()) {
            //A A* = a A(a) A* | A_no_a A*
            Factor.PullInfo tmp = new Factor(tree).pull(regex.node, sym);
            /*if (isEpsilon()) {

            }*/
        }
        //ques
        //A?
        Factor.PullInfo info = pull(regex.node, sym);
        if (info != null) {
            //A?: (a* A1)? = a* A1 | €
            info.zero = new Regex(info.zero, "?");
            return info;
        }
        return null;
    }

    boolean isEpsilon(Node node) {
        return Helper.first(node, tree, true).isEmpty();
    }

}
