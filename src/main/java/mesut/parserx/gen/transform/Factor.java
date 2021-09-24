package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Factor extends SimpleTransformer {

    public static boolean keepFactor = true;
    public static boolean debug = false;
    public static boolean factorSequence = true;
    public static boolean allowRecursion = false;
    public boolean any;
    HashMap<String, PullInfo> cache = new HashMap<>();
    boolean modified;
    RuleDecl curRule;
    CountingMap2<RuleDecl, Name> factorCount = new CountingMap2<>();
    HashSet<RuleDecl> declSet = new HashSet<>();//new rules produced by this class

    public Factor(Tree tree) {
        super(tree);
    }

    public static Name encode(Name name) {
        StringBuilder sb = new StringBuilder(name.name);
        sb.append("_");
        for (int i = 0; i < name.args.size(); i++) {
            //todo arg of arg?
            sb.append(name.args.get(i).name);
            if (i < name.args.size() - 1) {
                sb.append("_");
            }
        }
        Name res = new Name(sb.toString(), false);
        res.args = new ArrayList<>(name.args);
        return res;
    }

    public static Name common(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        //rule has higher priority
        Name res = null;
        for (Name name : copy) {
            if (!name.isEpsilon()) {
                if (name.isRule()) {
                    return name;
                }
                else {
                    res = name;
                }
            }
        }
        return res;
    }

    public Name commonSym(Node n1, Node n2) {
        Set<Name> s1 = first(n1);
        Set<Name> s2 = first(n2);
        return common(s1, s2);
    }

    String factorName(Name sym) {
        return sym.name + "f" + factorCount.get(curRule, sym);
    }

    public void factorize() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i);
            curRule = decl;
            modified = false;
            factorRule(decl);
            if (modified) {
                any = true;
                //restart if any modification happens
                i = 0;
                if (debug) {
                    tree.printRules();
                    //System.out.println(decl);
                }
            }
            else {
                i++;
            }
        }
    }

    private void factorRule(RuleDecl decl) {
        if (allowRecursion || !first(decl.rhs).contains(decl.ref())) {
            decl.rhs = transformNode(decl.rhs, decl);
        }
    }

    //factor or
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
            Set<Name> s1 = first(or.get(i));
            for (int j = i + 1; j < or.size(); j++) {
                Set<Name> s2 = first(or.get(j));
                Name sym = common(s1, s2);
                if (sym == null) continue;
                sym = sym.copy();
                //sym.astInfo.isFactor = true;
                //sym.astInfo.factorName = factorName(sym);
                if (debug)
                    System.out.printf("factoring %s in %s\n", sym, curRule.name);
                modified = true;
                PullInfo info = pull(or, sym);
                Group g = new Group(info.one);
                g.astInfo.isFactorGroup = true;
                Node one = Sequence.of(sym, g);
                if (info.zero == null) {
                    return one;
                }
                else {
                    return new Or(one, info.zero).normal();
                }
            }
        }
        return or;
    }

    //factor sequence
    @Override
    public Node transformSequence(Sequence s, Node parent) {
        Node node = super.transformSequence(s, parent);
        if (!factorSequence) {
            return node;
        }
        if (node.isSequence()) {
            s = node.asSequence();
        }
        else {
            return node;
        }
        //A B needs factoring if A can be empty
        //A B -> A_no_eps B | B
        Node A = s.first();
        if (Helper.canBeEmpty(A, tree)) {
            Node B = Helper.trim(s);
            Set<Name> s1 = first(A);
            Set<Name> s2 = first(B);
            if (common(s1, s2) != null) {
                Node trimmed = new Epsilons(tree).trim(A);
                return transformOr(new Or(Sequence.of(trimmed, B), B), parent);
            }
        }
        return s;
    }

    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doesn't start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Name sym) {
        if (sym.astInfo.isFactored) {
            throw new RuntimeException("factored sym");
        }
        if (!first(node).contains(sym)) {
            throw new RuntimeException("can't pull " + sym + " from " + node);
        }
        if (sym.astInfo.factorName == null) {
            sym.astInfo.isFactor = true;
            sym.astInfo.factorName = factorName(sym);
        }
        PullInfo info = new PullInfo();
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

    //pull sym from rule
    public PullInfo pullRule(Name name, Name sym) {
        if (debug)
            System.out.println("pullRule " + name + " sym=" + sym);
        PullInfo info = new PullInfo();
        String key = name + "-" + sym;
        if (cache.containsKey(key)) {
            //throw new RuntimeException("cached factor " + name);
            return cache.get(key);
        }
        cache.put(key, info);
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
        zeroName.astInfo = name.astInfo.copy();
        zeroName.astInfo.isPrimary = true;

        Name oneName = name.copy();
        oneName.astInfo = name.astInfo.copy();
        oneName.args.add(sym);

        info.one = oneName;

        if (tree.getRule(zeroName) == null && tree.getRule(oneName) == null) {
            PullInfo tmp = pull(decl.rhs, sym);

            RuleDecl oneDecl = oneName.makeRule();
            oneDecl.rhs = tmp.one.normal();
            oneDecl.retType = decl.retType;
            oneDecl.isRecursive = decl.isRecursive;
            tree.addRule(oneDecl);
            declSet.add(oneDecl);

            if (tmp.zero != null) {
                RuleDecl zeroDecl = zeroName.makeRule();
                zeroDecl.rhs = tmp.zero.normal();
                zeroDecl.retType = decl.retType;
                zeroDecl.isRecursive = decl.isRecursive;
                tree.addRule(zeroDecl);
                declSet.add(zeroDecl);
                info.zero = zeroName;
            }
        }
        return info;
    }

    PullInfo pullSeq(final Sequence s, Name sym) {
        PullInfo info = new PullInfo();
        //E=A B
        Node A = s.first();
        Node B = Helper.trim(s);
        if (Helper.start(A, sym, tree)) {
            PullInfo p1 = pull(A, sym);
            if (Helper.canBeEmpty(A, tree)) {
                //A B = A_noe B | B
                Node no = new Epsilons(tree).trim(A);
                Node B2 = B.copy();
                B2.astInfo.code = s.astInfo.code;
                Sequence se = new Sequence(no, B.copy());
                se.astInfo.code = s.astInfo.code;
                info = pull(new Or(se, B2), sym);
            }
            else {
                //A no empty
                //(a A1 | A0) B
                //a A1 B | A0 B
                if (p1.zero != null) {
                    info.zero = Sequence.of(p1.zero, B);
                    info.zero.astInfo.code = s.astInfo.code;
                }
                info.one = Sequence.of(p1.one, B);
                info.one.astInfo.code = s.astInfo.code;
            }
        }
        else {
            //A empty,B starts
            if (A.isName() && A.astInfo.isFactored) {
                PullInfo tmp = pull(B, sym);
                if (tmp.zero != null) {
                    info.zero = Sequence.of(A, tmp.zero);
                    if (s.astInfo.code != null) {
                        info.zero.astInfo.code = s.astInfo.code;
                    }
                    else {
                        info.zero.astInfo.code = tmp.zero.astInfo.code;
                    }
                }
                info.one = Sequence.of(A, tmp.one);
                if (s.astInfo.code != null) {
                    info.one.astInfo.code = s.astInfo.code;
                }
                else {
                    info.one.astInfo.code = tmp.one.astInfo.code;
                }
            }
            else {
                //A B=A_noe B | B
                Node no = new Epsilons(tree).trim(A);
                Node se = Sequence.of(no, B);
                se.astInfo = s.astInfo.copy();
                Name sym0 = sym.copy();
                sym0.astInfo.code = s.astInfo.code;
                Node B2 = B.copy();
                B2.astInfo.code = s.astInfo.code;
                info = pullOr(new Or(se, B2), sym0);
            }
        }
        /*if (info.zero != null) {
            info.zero.astInfo.code = s.astInfo.code;
        }
        info.one.astInfo.code = s.astInfo.code;*/
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
                pi.one.astInfo.code = ch.astInfo.code;
                if (pi.zero != null) {
                    zero.add(pi.zero);
                    pi.zero.astInfo.code = ch.astInfo.code;
                }
            }
            else {
                zero.add(ch);
            }
        }
        if (zero.size() > 0) {
            info.zero = zero.normal();
            //info.zero = zero;
        }
        info.one = one.normal();
        //info.one = one;
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
            //A* = A+ | €
            Regex plus = new Regex(regex.node, "+");
            return pull(new Or(plus, new Epsilon()), sym);
        }
        return info;
    }

    Set<Name> first(Node node) {
        Set<Name> set = new HashSet<>();
        Helper.first(node, tree, true, set);
        return set;
    }

    public static class PullInfo {
        public Node one;//after factor
        public Node zero;//other
    }
}
