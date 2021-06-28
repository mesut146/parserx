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

    @Override
    public Node transformOr(Or or, Node parent) {
        Node node = super.transformOr(or, parent);
        if (node.isOr()) {
            or = node.asOr();
        }
        else {
            return node;
        }
        for (int i = 0; i < or.size(); i++) {
            Set<Name> s1 = Helper.first(or.get(i), tree, true);
            for (int j = 0; j < or.size(); j++) {
                Set<Name> s2 = Helper.first(or.get(i), tree, true);
                Name sym = conf(s1, s2);
                if (sym != null) {
                    PullInfo info = pull(or, sym);
                    Node one = Sequence.of(sym, info.one);
                    return new Or(one, info.zero).normal();
                }
            }
        }
        return or;
    }

    @Override
    public Node transformSequence(Sequence s, Node parent) {
        Node node = super.transformSequence(s, parent);
        if (node.isSequence()) {
            s = node.asSequence();
        }
        else {
            return node;
        }
        Node A = s.first();
        if (Helper.canBeEmpty(A, tree)) {
            Node B = Helper.trim(s);
            Set<Name> s1 = Helper.first(A, tree, true);
            Set<Name> s2 = Helper.first(B, tree, true);
            Name sym = conf(s1, s2);
            if (sym != null) {
                return transformOr(new Or(Sequence.of(new Epsilons(tree).trim(A), B), B), parent);
            }
        }
        return s;
    }

    public void handle() {
        boolean modified = false;
        while (true) {
            for (int i = 0; i < tree.rules.size(); i++) {
                RuleDecl decl = tree.rules.get(i);
                while (modified = factor(decl)) ;

                if (modified) {
                    //restart if any modification happens
                    break;
                }
            }
            if (!modified) {
                break;
            }
        }
    }

    private boolean factor(RuleDecl decl) {
        decl.rhs = transformNode(decl.rhs, decl);
        return false;
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

    Name conf(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        if (copy.isEmpty()) return null;
        return copy.iterator().next();
    }


    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doest start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Name sym) {
        if (!Helper.first(node, tree, true).contains(sym)) {
            throw new RuntimeException("can't pull");
        }
        PullInfo info = new PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.equals(sym)) {
                info.one = new Epsilon();
            }
            else if (name.isRule()) {
                RuleDecl decl = tree.getRule(name);
                //check if already pulled before
                if (decl.args.size() == 1 && decl.args.get(0).equals(sym)) {
                    info.zero = new Name("X");
                    info.one = decl.ref();
                    return info;
                }
                PullInfo tmp = pull(decl.rhs, sym);
                RuleDecl oneDecl = new RuleDecl(decl.name, tmp.one.normal());
                if (tmp.zero != null) {
                    RuleDecl zeroDecl = new RuleDecl(decl.name + "0", tmp.zero.normal());
                    tree.addRule(zeroDecl);
                    info.zero = zeroDecl.ref();
                }
                oneDecl.args.add(sym);
                tree.addRule(oneDecl);

                info.one = oneDecl.ref();
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

    PullInfo pullSeq(Sequence s, Name sym) {
        PullInfo info = new PullInfo();
        //E=A B
        Node A = s.first();
        Node B = Helper.trim(s);
        if (Helper.start(A, sym, tree)) {
            PullInfo p1 = pull(A, sym);
            if (Helper.canBeEmpty(A, tree)) {
                if (Helper.start(B, sym, tree)) {
                    Node no = new Epsilons(tree).trim(A);
                    //A B = A_no B | B =
                    return pull(new Or(Sequence.of(no, B), B), sym);
                }
                else {
                    //A B = (a A1 | A0) B = a A1 B | A0 B
                    info.zero = Sequence.of(p1.zero, B);
                    info.one = Sequence.of(p1.one, B);
                }
            }
            else {
                //A no empty
                //(a A1 | A0) B
                //E1: A1 B , E0: A0 B
                if (p1.zero != null) {
                    info.zero = makeSeq(p1.zero, B);
                }
                info.one = makeSeq(p1.one, B);
            }
        }
        else {
            //A empty,B starts
            //A_no B | B
            Node no = new Epsilons(tree).trim(A);
            return pull(new Or(Sequence.of(no, B), B), sym);
        }
        return info;
    }

    PullInfo pullOr(Or or, Name sym) {
        PullInfo info = new PullInfo();
        //A | B | C
        //a A1 | A0 | a B1 | B0 | C
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
        info.zero = zero.normal();
        info.one = one.normal();
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
            return n2.normal();
        }
        if (n2 == null) {
            return n1.normal();
        }
        return new Sequence(n1, n2).normal();
    }

    public static class PullInfo {
        public Node one;
        public Node zero;
    }
}
