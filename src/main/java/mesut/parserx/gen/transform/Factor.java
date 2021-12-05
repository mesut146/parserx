package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;

import java.util.*;

public class Factor extends Transformer {

    public static boolean keepFactor = true;
    public static boolean debug = false;
    public static boolean debugPull = false;
    public static boolean factorSequence = true;
    public HashSet<RuleDecl> declSet = new HashSet<>();//new rules produced by this class
    public RuleDecl curRule;
    HashMap<String, PullInfo> cache = new HashMap<>();
    boolean modified;
    CountingMap2<RuleDecl, Name> factorCount = new CountingMap2<>();
    HashMap<Name, Name> senderMap = new HashMap<>();

    public Factor(Tree tree) {
        super(tree);
    }

    public static Name common(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        Name res = null;
        for (Name name : copy) {
            if (!name.isEpsilon()) {
                if (name.isRule()) {
                    //rule has higher priority
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
        if (s.astInfo.which == -1) return;
        for (Node ch : s.asSequence()) {
            if (ch.astInfo.which != -1) {
                throw new RuntimeException("");
            }
        }
    }

    public Name common(Node a, Node b) {
        Set<Name> s1 = first(a);
        Set<Name> s2 = first(b);
        Set<Name> common = new HashSet<>(s1);
        common.retainAll(s2);
        Name res = null;
        if (a.isName() && common.contains(a.asName())) {
            return a.asName();
        }
        if (b.isName() && common.contains(b.asName())) {
            return b.asName();
        }
        if (a.isRegex() && a.asRegex().node.asName().equals(b)) {
            return b.asName();
        }
        if (b.isRegex() && b.asRegex().node.asName().equals(a)) {
            return a.asName();
        }
        for (Name name : common) {
            if (name.isRule()) {
                //rule has higher priority
                return name;
            }
            else {
                res = name;
            }
        }
        return res;
    }

    public String factorName(Name sym) {
        return sym.name + "f" + factorCount.get(curRule, sym);
    }

    //factor or
    @Override
    public Node visitOr(Or or, Void parent) {
        Node node = super.visitOr(or, parent);
        if (node.isOr()) {
            or = node.asOr();
        }
        else {
            return node;
        }
        for (int i = 0; i < or.size(); i++) {
            for (int j = i + 1; j < or.size(); j++) {
                Name sym = common(or.get(i), or.get(j));
                if (sym == null) continue;
                sym = sym.copy();
                sym.astInfo = new AstInfo();
                sym.astInfo.isFactor = true;
                sym.astInfo.factorName = factorName(sym);
                if (debug) {
                    System.out.printf("factoring %s in %s\n", sym, curRule.ref);
                }
                modified = true;
                PullInfo info = pull(or, sym);
                Group g = new Group(info.one);
                Node one = new Sequence(sym, g);
                if (info.zero == null) {
                    return one;
                }
                else {
                    return makeOr(one, info.zero);
                }
            }
        }
        return or;
    }

    Or makeOr(Node a, Node b) {
        if (a.isOr()) {
            if (a.astInfo.which != -1) {
                throw new RuntimeException();
            }
            Or res = new Or(a.asOr().list);
            res.add(b);
            return res;
        }
        else if (b.isOr()) {
            if (b.astInfo.which != -1) {
                throw new RuntimeException();
            }
            Or res = new Or(b.asOr().list);
            res.add(a);
            return res;
        }
        return new Or(a, b);
    }

    //factor sequence
    @Override
    public Node visitSequence(Sequence s, Void parent) {
        Node node = super.visitSequence(s, parent);
        if (!factorSequence) {
            return node;
        }
        if (node.isSequence()) {
            s = node.asSequence();
        }
        else {
            return node;
        }
        //A B needs factoring if A can be empty and A , B have common factor which makes A greedy
        //A B -> A_no_eps B | B
        Node A = s.first();
        if (Helper.canBeEmpty(A, tree)) {
            Node B = Helper.trim(s);
            Set<Name> s1 = first(A);
            Set<Name> s2 = first(B);
            if (common(s1, s2) != null) {
                Node trimmed = Epsilons.trim(A, tree);
                return visitOr(new Or(new Sequence(trimmed, B), B), parent);
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
        if (!FirstSet.start(node, sym, tree)) {
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
            Group group = node.asGroup();
            info = pull(group.node, sym);
            info.one = new Group(info.one);
            info.one.astInfo = group.astInfo.copy();
            if (info.zero != null) {
                info.zero = new Group(info.zero);
                info.zero.astInfo = group.astInfo.copy();
            }
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

    Name getBase(Name name) {
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
        if (debugPull)
            System.out.println("pullRule " + name + " sym=" + sym);
        PullInfo info = new PullInfo();
        String key = name.name + "-" + sym;
        if (cache.containsKey(key)) {
            info = cache.get(key);
            info.one = info.one.copy();
            info.one.astInfo = name.astInfo.copy();
            if (info.zero != null) {
                info.zero = info.zero.copy();
                info.zero.astInfo = name.astInfo.copy();
            }
            return info;
        }
        cache.put(key, info);
        //check if already pulled before
        //Name zeroName = new Name(base.name + nameMap.get(base.name));
        Name zeroName = new Name(name.name + "_no_" + sym.name);
        zeroName.args = new ArrayList<>(name.args);
        zeroName.astInfo = name.astInfo.copy();

        Name oneName = new Name(tree.getName(getBase(name).name));
        //Name oneName = new Name(base.name + "_with_" + sym);
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
        oneDecl.rhs = tmp.one;
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);
        declSet.add(oneDecl);
        senderMap.put(oneName, senderMap.get(name));

        if (tmp.zero != null) {
            RuleDecl zeroDecl = zeroName.makeRule();
            zeroDecl.rhs = tmp.zero;
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            declSet.add(zeroDecl);
            senderMap.put(zeroName, senderMap.get(name));
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
            if (Helper.canBeEmpty(A, tree) && Helper.start(B, sym, tree)) {
                //(a A(a) | A_no_a) B
                //a A(a) B | A_no_a B
                return pull(new Or(new Sequence(ai.zero, B), new Sequence(ai.one, B)), sym);
                /*if (Helper.canBeEmpty(ai.zero, tree)) {
                    //a A(a) B | A_no_a_eps B | A_no_a_noe B
                    //a A(a) B | A_no_a_eps B | A_no_a_noe B
                    info.zero = new Sequence(ai.zero, B);
                    info.one = new Sequence(ai.one, B);
                }
                else {
                    info.zero = new Sequence(ai.zero, B);
                    info.one = new Sequence(ai.one, B);
                }*/
            }
            //(a A1 | A0) B
            //a A(a) B | A_no_a B
            if (ai.zero != null) {
                info.zero = new Sequence(ai.zero, B.copy());
                info.zero.astInfo = s.astInfo.copy();
            }
            info.one = new Sequence(ai.one, B.copy());
            info.one.astInfo = s.astInfo.copy();
            //check(info.one);
            //check(info.zero);
            return info;

        }
        else {
            //A empty,B starts
            //A B=A_noe B | A_eps B
            //A_noe B | a A_eps B(a) | A_eps B_no_a
            Epsilons.Info a1 = Epsilons.trimInfo(A, tree);
            PullInfo pb = pull(B, sym);
            Node s1 = a1.noEps == null ? null : Sequence.make(a1.noEps, B.copy());
            //Node s2 = a1.eps.isEpsilon() ? pb.one : new Sequence(a1.eps, pb.one);
            Node s2 = new Sequence(a1.eps, pb.one);
            //Node s3 = pb.zero == null ? null : (a1.eps.isEpsilon() ? pb.zero : new Sequence(a1.eps, pb.zero));
            Node s3 = pb.zero == null ? null : Sequence.make(a1.eps, pb.zero);

            if (s2.astInfo.which == -1) s2.astInfo = s.astInfo.copy();

            if (s1 == null) {
                info.zero = s3;
            }
            else {
                s1.astInfo = s.astInfo.copy();
                if (s3 == null) {
                    info.zero = s1;
                }
                else {
                    if (s3.astInfo.which == -1) s3.astInfo = s.astInfo.copy();
                    info.zero = Or.make(s1, s3);
                }
            }
            info.one = s2;
            check(s1);
            check(s2);
            check(s3);
        }
        return info;
    }

    PullInfo pullOr(Or or, Name sym) {
        PullInfo info = new PullInfo();
        //A | B | C
        //a A1 | A0 | a B1 | B0 | C
        List<Node> one = new ArrayList<>();
        List<Node> zero = new ArrayList<>();
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
        if (zero.size() > 0) {
            info.zero = Or.make(zero);
        }
        info.one = Or.make(one);
        return info;
    }

    PullInfo pullRegex(Regex regex, Name sym) {
        if (regex.isOptional()) {
            PullInfo info = new PullInfo();
            //A?=A | € = sym A1 | A0 | €
            PullInfo pi = pull(regex.node, sym);
            info.one = withAst(pi.one, regex);
            if (pi.zero != null) {
                info.zero = withAst(new Regex(pi.zero, "?"), regex);
            }
            else {
                info.zero = new Epsilon();
            }
            return info;
        }
        else if (regex.isPlus()) {
            //A+=A A*
            Node star = withAst(new Regex(regex.node, "*"), regex);
            if (regex.astInfo.isFactor) {
                star.astInfo.loopExtra = sym.astInfo.factorName;
            }
            Sequence s = new Sequence(regex.node, star);
            return pull(s, sym);
        }
        else {
            //A* = A+ | €
            Node plus = withAst(new Regex(regex.node, "+"), regex);
            if (regex.astInfo.isFactor) {
                plus.astInfo.loopExtra = sym.astInfo.factorName;
            }
            return pull(new Or(plus, new Epsilon()), sym);
        }
    }

    private Node withAst(Node node, Regex other) {
        if (node.astInfo.isFactored) return node;
        node.astInfo = other.astInfo.copy();
        return node;
    }

    //return non-empty symbols
    public Set<Name> first(Node node) {
        Set<Name> set = FirstSet.firstSet(node, tree);
        //if only itself
        if (node.isName() && node.asName().isRule() && set.size() == 1) {
            if (set.iterator().next().equals(node)) {
                return new HashSet<>();
            }
            else {
                return set;
            }
        }
        //remove epsilon rules
        for (Iterator<Name> it = set.iterator(); it.hasNext(); ) {
            Name sym = it.next();
            if (sym.isRule() && FirstSet.isEmpty(sym, tree)) {
                it.remove();
            }
        }
        return set;
    }

    public static class PullInfo {
        public Node one;//after factor
        public Node zero;//other
    }
}
