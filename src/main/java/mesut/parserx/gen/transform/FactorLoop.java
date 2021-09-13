package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class FactorLoop {
    Tree tree;

    public FactorLoop(Tree tree) {
        this.tree = tree;
    }

    Factor.PullInfo pull(Node node, Name sym) {
        if (!Helper.start(node, sym, tree)) {
            return null;
        }
        return null;
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
        return null;
    }

    Factor.PullInfo pullName(Name name, Name sym) {
        if (name.isRule()) {
            RuleDecl decl = tree.getRule(name);
            return pull(decl.rhs, sym);
        }
        return null;
    }

    Factor.PullInfo pullRegex(Regex regex, Name sym) {
        if (regex.isStar()) {
            Factor.PullInfo info = new Factor(tree).pull(regex.node, sym);
            //A* = (a A(a) | A_no_a)* (a A(a))* A_no_a A* if A(a) is epsilon
            //A* = (a A_a_noe(a) | a | A_no_a)*
            //A* = a* (a A_a_noe(a) | A_no_a)
            /*
            A(a): a(a) b?
            A: a b?
            B: A*
            B: a A(a) A* | €
            B: a A_a_noe(a) A* | a A* | €
            B: a A_a_noe(a) A* | a B | €
            B: a

             */
        }
        return null;
    }

    Factor.PullInfo pullGroup(Group gr, Name sym) {
        return pull(gr.node, sym);
    }


}
