package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Factor extends SimpleTransformer {

    Tree tree;

    public Factor(Tree tree) {
        this.tree = tree;
    }

    public void handle() {
        for (RuleDecl decl : tree.rules) {
            while (factor(decl)) {

            }
        }
    }

    Name conf(Set<Name> s1, Set<Name> s2) {
        Set<Name> tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        if (tmp.isEmpty()) return null;
        return tmp.iterator().next();
    }

    Name hasDup(List<Name> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).equals(list.get(j))) {
                    return list.get(i);
                }
            }
        }
        return null;
    }

    boolean factor(RuleDecl decl) {
        List<Name> list = Helper.firstList(decl.rhs, tree);
        Name cc = hasDup(list);
        if (cc != null && cc.isToken) {
            PullInfo p = pull(decl.rhs, cc);
            decl.rhs = new Or(new Sequence(cc, p.one), p.zero);
            return true;
        }
        /*if (decl.rhs.isOr()) {
            Or or = decl.rhs.asOr();
            for (int i = 0; i < or.size(); i++) {
                Set<Name> s1 = Helper.first(or.get(i), tree, true);
                for (int j = i + 1; j < or.size(); j++) {
                    Set<Name> s2 = Helper.first(or.get(j), tree, true);
                    Name common = conf(s1, s2);
                    if (common != null) {
                        PullInfo p = pull(or, common);
                        decl.rhs = new Or(new Sequence(common, p.one), p.zero);
                        return true;
                    }
                }
            }
        }
        else if (decl.rhs.isSequence()) {

        }*/
        return false;
    }

    PullInfo pullSeq(Sequence s, Name sym) {
        PullInfo info = new PullInfo();
        //E=A B
        Node A = s.first();
        Node B = Helper.trim(s);
        if (Helper.start(A, sym, tree)) {
            PullInfo p1 = pull(A, sym);
            if (Helper.canBeEmpty(A, tree)) {
                if (Helper.start(B, sym, tree)) {
                    if (Helper.canBeEmpty(p1.zero, tree)) {
                        throw new RuntimeException("A0 empty");
                    }
                    PullInfo p2 = pull(B, sym);
                    //A B = (a A1 | A0) (a B1 | B0) = a A1 a B1 | a A1 B0 | A0 a B1 | A0 B0
                    info.one = new Or(new Sequence(p1.one, sym, p2.one).normal(), new Sequence(p1.one, p2.zero).normal());
                    info.zero = new Or(new Sequence(p1.zero, sym, p2.one).normal(), new Sequence(p1.zero, p2.zero).normal());
                }
                else {
                    //A B = (a A1 | A0) B = a A1 B | A0 B
                    info.zero = Sequence.of(p1.zero, B);
                    info.one = Sequence.of(p1.one, B);
                }
            }
            else {
                //E1: A1 B , E0: A0 B
                if (p1.zero != null) {
                    info.zero = makeSeq(p1.zero, B);
                }
                info.one = makeSeq(p1.one, B);
            }
        }
        else {
            //A must be empty,B starts
            //E1:
            PullInfo p2 = pull(B, sym);
            info.one = p2.one;
            if (p2.zero != null) {
                info.zero = A;
            }
        }
        return info;
    }

    PullInfo pullOr(Or or, Name sym) {
        PullInfo info = new PullInfo();
        //A | B
        //a A1 | A0 | a
        Or one = new Or();
        Or zero = new Or();
        for (Node ch : or) {
            if (Helper.start(ch, sym, tree)) {
                PullInfo pi = pull(ch, sym);
                one.add(pi.one);
                if (pi.zero != null) {
                    zero.add(pi.zero);
                }
            }
            else {
                zero.add(ch);
            }
        }
        if (zero.size() != 0) {
            info.zero = zero.normal();
        }
        if (one.size() != 0) {
            info.one = one.normal();
        }
        return info;
    }

    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doest start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Name sym) {
        if (!Helper.first(node, tree, true).contains(sym)) {
            //info.not = node;
            //return info;
            throw new RuntimeException("can't pull");
        }
        PullInfo info = new PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.equals(sym)) {
                info.one = new Epsilon();
                return info;
            }
            if (name.isRule()) {
                RuleDecl decl = tree.getRule(name.name);
                PullInfo tmp = pull(decl.rhs, sym);
                return tmp;
            }
        }
        else if (node.isGroup()) {
            Or.newLine = false;
            info = pull(node.asGroup().node, sym);
        }
        else if (node.isOr()) {
            info = pullOr(node.asOr(), sym);
        }
        else if (node.isSequence()) {
            //E=A B
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

    PullInfo pullRegex(Regex regex, Name sym) {
        PullInfo info = new PullInfo();
        if (regex.isOptional()) {
            PullInfo pi = pull(regex.node, sym);
            if (pi.one != null) {
                info.one = pi.one;
            }
            if (pi.zero != null) {
                info.zero = new Or(pi.zero, new Epsilon());
            }
        }
        else if (regex.isPlus()) {
            //A+=A A*
            Sequence s = new Sequence(regex.node, new Regex(regex.node, "*"));
            return pull(s.normal(), sym);
        }
        else {
            //A*=A+?
            Regex star = new Regex(regex.node, "+");
            return pull(new Or(star, new Epsilon()), sym);
        }
        return info;
    }

    Node makeSeq(Node n1, Node n2) {
        if (n1 == null) {
            return n2;
        }
        if (n2 == null) {
            return n1;
        }
        return new Sequence(n1, n2).normal();
    }

    public static class PullInfo {
        public Node one;
        public Node zero;
    }
}
