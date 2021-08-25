package mesut.parserx.gen;

import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Factor2 extends SimpleTransformer {

    public static boolean keepFactor = true;
    Tree tree;
    HashMap<Name, PullInfo> cache = new HashMap<>();
    boolean modified;
    RuleDecl curRule;

    public Factor2(Tree tree) {
        this.tree = tree;
    }

    public static Name encode(Name name) {
        return new Name(name.name + "_" + NodeList.join(name.args, "_"), false);
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
            for (int j = i + 1; j < or.size(); j++) {
                Set<Name> s2 = Helper.first(or.get(j), tree, true);
                Name sym = conf(s1, s2);
                if (sym != null) {
                    System.out.printf("factoring %s in %s\n", sym, curRule.name);
                    modified = true;
                    PullInfo info = pull(or, sym);
                    Node one = Sequence.of(sym, info.one);
                    if (info.zero == null) {
                        return one;
                    }
                    else {
                        return new Or(one, info.zero).normal();
                    }
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
        while (true) {
            modified = false;
            for (int i = 0; i < tree.rules.size(); i++) {
                RuleDecl decl = tree.rules.get(i);
                curRule = decl;
                while (true) {
                    if (factor(decl)) {
                        modified = true;
                    }
                    else {
                        break;
                    }
                }
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

    //return common sym in two set
    Name conf(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        //rule has higher priority
        for (Name name : copy) {
            if (!name.isEpsilon() && name.isRule()) return name;
        }
        //token finally
        for (Name name : copy) {
            if (!name.isEpsilon()) return name;
        }
        return null;
    }

    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doesn't start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Regex sym) {
        //todo sym factored
        //System.out.println("pull:" + node + " sym:" + sym);
        if (!Helper.first(node, tree, true).contains(sym.node)) {
            throw new RuntimeException("can't pull " + sym + " from " + node);
        }
        PullInfo info = new PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.equals(sym)) {
                if (keepFactor) {
                    name = name.copy();
                    name.factored = true;
                    info.one = name;
                }
                else {
                    info.one = new Epsilon();
                }
            }
            else if (name.isRule()) {
                if (cache.containsKey(name)) {
                    return cache.get(name);
                }
                cache.put(name, info);
                RuleDecl decl = tree.getRule(name);
                //check if already pulled before

                Name zeroName;
                if (name.args.isEmpty()) {
                    zeroName = new Name(name.name, false);
                }
                else {
                    //encode args
                    zeroName = encode(name);
                }

                zeroName.name += "_no_" + sym.name;
                Name oneName = name.copy();
                oneName.args.add(sym);

                info.zero = zeroName;
                info.one = oneName;

                if (tree.getRule(zeroName) == null && tree.getRule(oneName) == null) {
                    PullInfo tmp = pull(decl.rhs, sym);

                    RuleDecl oneDecl = oneName.makeRule();
                    oneDecl.rhs = tmp.one.normal();
                    tree.addRule(oneDecl);

                    if (tmp.zero != null) {
                        RuleDecl zeroDecl = zeroName.makeRule();
                        zeroDecl.rhs = tmp.zero.normal();
                        tree.addRule(zeroDecl);
                    }
                }
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
                    //A B = A_noe B | B
                    return pull(new Or(Sequence.of(no, B), B), sym);
                }
                else {
                    //A B = (sym A1 | A0) B = sym A1 B | A0 B
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
            if (A.isName() && A.asName().factored) {
                PullInfo tmp = pull(B, sym);
                if (tmp.zero != null)
                    info.zero = Sequence.of(A, tmp.zero);
                info.one = Sequence.of(A, tmp.one);
            }
            else {
                //A B=A_noe B | B
                Node no = new Epsilons(tree).trim(A);
                return pull(new Or(Sequence.of(no, B), B), sym);
            }
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
            //A?=A | € = sym A1 | A0 | €
            PullInfo pi = pull(regex.node, sym);
            if (pi.one != null) {
                info.one = pi.one;
            }
            if (pi.zero != null) {
                //info.zero = new Or(pi.zero, new Epsilon());
                info.zero = new Regex(pi.zero, "?");
            }
        }
        else if (regex.isPlus()) {
            //A+=A A*
            Sequence s = new Sequence(regex.node, new Regex(regex.node, "*"));
            return pull(s.normal(), sym);
        }
        else {
            //A*=A+?-A+|€
            Regex plus = new Regex(regex.node, "+");
            return pull(new Or(plus, new Epsilon()), sym);
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
