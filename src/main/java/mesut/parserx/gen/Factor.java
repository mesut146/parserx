package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Factor {

    Tree tree;
    List<RuleDecl> results = new ArrayList<>();

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

    boolean factor(RuleDecl decl) {
        if (decl.rhs.isOr()) {
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
        return false;
    }

    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doest start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Name sym) {
        PullInfo info = new PullInfo();
        if (!Helper.first(node, tree, true).contains(sym)) {
            //info.not = node;
            //return info;
            throw new RuntimeException("can't pull");
        }
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
            //A | B
            //a A1 | A0 | a
            Or one = new Or();
            Or zero = new Or();
            for (Node ch : node.asOr()) {
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
        }
        else if (node.isSequence()) {
            //E=A B
            Sequence s = node.asSequence();
            Node first = s.first();
            Node sec = Helper.trim(s);
            if (Helper.start(first, sym, tree)) {
                if (Helper.canBeEmpty(first, tree)) {
                    if (Helper.start(sec, sym, tree)) {

                    }

                }
                else {
                    PullInfo p1 = pull(first, sym);
                    if (p1.zero != null) {
                        info.zero = makeSeq(p1.zero, sec);
                    }
                    info.one = makeSeq(p1.one, sec);
                }
            }
            else {
                //first must be empty
                PullInfo p2 = pull(sec, sym);
                info.one = p2.one;
                if (p2.zero != null) {
                    info.zero = first;
                }
            }
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
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
                return pull(s, sym);
            }
            else {
                //A*=A+?
                Regex star = new Regex(regex.node, "+");
                return pull(new Or(star, new Epsilon()), sym);
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass());
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
