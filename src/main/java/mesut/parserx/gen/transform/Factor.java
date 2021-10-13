package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.gen.ll.AstInfo;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap;
import mesut.parserx.utils.CountingMap2;

import java.util.*;

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
    public HashSet<RuleDecl> declSet = new HashSet<>();//new rules produced by this class
    CountingMap<String> nameMap = new CountingMap<>();
    HashMap<Name, Name> senderMap = new HashMap<>();

    public Factor(Tree tree) {
        super(tree);
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

    public static void check(Node s) {
        if (s == null || !s.isSequence()) return;
        if (s.astInfo.code == null) return;
        for (Node ch : s.asSequence()) {
            if (ch.astInfo.code != null) {
                throw new RuntimeException("");
            }
        }
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
                    //tree.printRules();
                    System.out.println(decl);
                }
            }
            else {
                i++;
            }
        }
    }

    private void factorRule(RuleDecl decl) {
        if (allowRecursion || !first(decl.rhs).contains(decl.ref)) {
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
                sym.astInfo = new AstInfo();
                //sym.astInfo.isFactor = true;
                //sym.astInfo.factorName = factorName(sym);
                if (debug) {
                    System.out.printf("factoring %s in %s\n", sym, curRule.ref);
                }
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
                Node trimmed = Epsilons.trim(A, tree);
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

    Name baseName(Name name) {
        if (senderMap.containsKey(name)) {
            return senderMap.get(name);
        }
        else {
            senderMap.put(name, name);
            return name;
        }
    }

    //pull sym from rule
    public PullInfo pullRule(Name name, Name sym) {
        if (debug)
            System.out.println("pullRule " + name + " sym=" + sym);
        PullInfo info = new PullInfo();
        String key = name + "-" + sym;
        if (cache.containsKey(key)) {
            info = cache.get(key);
            info.one.astInfo = name.astInfo.copy();
            if (info.zero != null)
                info.zero.astInfo = name.astInfo.copy();
            return info;
            //todo astinfo carried incorrectly
        }
        cache.put(key, info);
        //check if already pulled before
        Name base = baseName(name);
        Name zeroName = new Name(base.name + nameMap.get(base.name));
        zeroName.args = new ArrayList<>(name.args);
        zeroName.astInfo = name.astInfo.copy();
        zeroName.astInfo.isPrimary = true;

        Name oneName = new Name(base.name + nameMap.get(base.name));
        oneName.args = new ArrayList<>(name.args);
        oneName.args.add(sym);
        oneName.astInfo = name.astInfo.copy();
        info.one = oneName;
        //can fill zero by checking first set

        if (tree.getRule(oneName) != null) {
            if (tree.getRule(zeroName) != null) {
                info.zero = zeroName;
            }
            return info;
        }

        RuleDecl decl = tree.getRule(name);
        PullInfo tmp = pull(decl.rhs, sym);

        RuleDecl oneDecl = oneName.makeRule();
        oneDecl.rhs = tmp.one.normal();
        oneDecl.retType = decl.retType;
        oneDecl.isRecursive = decl.isRecursive;
        tree.addRuleBelow(oneDecl, decl);
        declSet.add(oneDecl);
        if (senderMap.containsKey(name)) {
            senderMap.put(oneName, senderMap.get(name));
        }
        else {
            senderMap.put(oneName, name);
        }

        if (tmp.zero != null) {
            RuleDecl zeroDecl = zeroName.makeRule();
            zeroDecl.rhs = tmp.zero.normal();
            zeroDecl.retType = decl.retType;
            zeroDecl.isRecursive = decl.isRecursive;
            tree.addRuleBelow(zeroDecl, decl);
            declSet.add(zeroDecl);
            if (senderMap.containsKey(name)) {
                senderMap.put(zeroName, senderMap.get(name));
            }
            else {
                senderMap.put(zeroName, name);
            }
            info.zero = zeroName;
        }
        return info;
    }

    PullInfo pullSeq(Sequence s, Name sym) {
        PullInfo info = new PullInfo();
        //A B
        Node A = s.first();
        Node B = Helper.trim(s);
        if (Helper.start(A, sym, tree)) {
            PullInfo ai = pull(A, sym);
            if (Helper.canBeEmpty(A, tree)) {
                if (Helper.start(B, sym, tree)) {
                    throw new RuntimeException("not yet");
                }
                else {
                    //A B = A_eps B | A_noe B
                    //a A(a) B | A_no_a B
                    Sequence s1 = new Sequence(ai.one, B.copy());
                    if (ai.zero != null) {
                        Sequence s2 = new Sequence(ai.zero, B.copy());
                        s2.astInfo.code = s.astInfo.code;
                        info.zero = s2;
                        check(s2);
                    }
                    s1.astInfo.code = s.astInfo.code;
                    info.one = s1;
                    check(s1);
                    return info;
                }
            }
            else {
                //A no empty
                //(a A1 | A0) B
                //a A1 B | A0 B
                if (ai.zero != null) {
                    info.zero = new Sequence(ai.zero, B);
                    info.zero.astInfo.code = s.astInfo.code;
                }
                info.one = Sequence.of(ai.one, B);
                info.one.astInfo.code = s.astInfo.code;
                check(info.one);
                check(info.zero);
            }
        }
        else {
            //A empty,B starts
            //A B=A_noe B | A_eps B
            //A_noe B | a A_eps B(a) | A_eps B_no_a
            Epsilons.Info a1 = Epsilons.trimInfo(A, tree);
            PullInfo pb = pull(B, sym);
            Node s1 = a1.noEps == null ? null : new Sequence(a1.noEps, B.copy());
            Node s2 = a1.eps.isEpsilon() ? pb.one : new Sequence(a1.eps, pb.one);
            Node s3 = pb.zero == null ? null : (a1.eps.isEpsilon() ? pb.zero : new Sequence(a1.eps, pb.zero));

            if (s2.astInfo.code == null) s2.astInfo.code = s.astInfo.code;

            if (s1 == null) {
                info.zero = s3;
            }
            else {
                s1.astInfo.code = s.astInfo.code;
                if (s3 == null) {
                    info.zero = s1;
                }
                else {
                    if (s3.astInfo.code == null) s3.astInfo.code = s.astInfo.code;
                    info.zero = new Or(s1, s3);
                }
            }
            info.one = s2;
            check(s1);
            check(s2);
            check(s3);
        }
        return info;
    }

    private Node withCode(Node node, Node astHolder) {
        node.astInfo.code = astHolder.astInfo.code;
        return node;
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
                //pi.one.astInfo.code = ch.astInfo.code;
                one.add(pi.one);
                if (pi.zero != null) {
                    zero.add(pi.zero);
                    //pi.zero.astInfo.code = ch.astInfo.code;
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
        //remove epsilon rules
        if (node.isName() && node.asName().isRule() && set.size() == 1) {
            if (set.iterator().next().equals(node)) {
                set.clear();
            }
        }
        else {
            for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
                Name sym = it.next();
                if (sym.isRule() && isEmpty(sym)) {
                    it.remove();
                }
            }
        }
        return set;
    }

    boolean isEmpty(Name node) {
        if (node.isRule()) {
            Set<Name> first = Helper.first(node, tree, true);
            if (first.isEmpty()) {
                return true;
            }
            if (first.size() == 1) {
                Name f = first.iterator().next();
                if (f.equals(node)) {
                    return true;
                }
                else {
                    return isEmpty(f);
                }
            }
        }
        return false;
    }

    public static class PullInfo {
        public Node one;//after factor
        public Node zero;//other
    }
}
