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
    public HashSet<RuleDecl> declSet = new HashSet<>();//new rules produced by this class
    public RuleDecl curRule;
    HashMap<String, PullInfo> cache = new HashMap<>();
    boolean modified;
    CountingMap2<RuleDecl, Name> factorCount = new CountingMap2<>();

    public Factor(Tree tree) {
        super(tree);
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


    public String factorName(Name sym) {
        return sym.name + "f" + factorCount.get(curRule, sym);
    }

    //factor or
    /*@Override
    public Node visitOr(Or or, Void parent) {
        if (true){
            throw new RuntimeException();
        }
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
                return factorOr(or, sym);
            }
        }
        return or;
    }*/

    Node factorOr(Or or, Name sym) {
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
            return Or.make(one, info.zero);
        }
    }

    //factor sequence
    /*@Override
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
            if (common(A, B) != null) {
                if (debug)
                    System.out.println("factoring greedy seq");
                Node trimmed = Epsilons.trim(A, tree);
                return visitOr(new Or(new Sequence(trimmed, B), B), parent);
            }
        }
        return s;
    }*/

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
                    if (name.astInfo.isFactor && !name.astInfo.isInLoop) {
                        sym.astInfo.factorName = name.astInfo.factorName;
                    }
                    else {
                        name.astInfo.factorName = sym.astInfo.factorName;
                    }
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

    //pull sym from rule
    public PullInfo pullRule(Name name, Name sym) {
        if (debugPull) {
            System.out.println("pullRule " + name + " sym=" + sym);
        }

        PullInfo info = new PullInfo();

        Name zeroName = tree.getFactorZero(name, sym);
        Name oneName = tree.getFactorOne(name, sym);
        info.one = oneName;

        //check if already pulled before
        if (tree.getRule(oneName) != null) {
            if (tree.getRule(zeroName) != null) {
                info.zero = zeroName;
            }
            return info;
        }

        RuleDecl decl = tree.getRule(name);
        if (FactorHelper.hasZero(decl.rhs, sym, tree)) {
            info.zero = zeroName;
        }

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
        else {
            cache.put(key, info);
        }

        Name symArg = sym.copy();

        Name oneRef = oneName.copy();
        oneRef.args.clear();
        for (Node arg : decl.ref.args) {
            oneRef.args.add(arg.copy());
        }
        oneRef.args.add(symArg);
        update(oneRef, symArg);//depends on name not oneRef
        if (name.astInfo.isPrimary) {
            oneRef.astInfo.isPrimary = false;
        }

        Name zeroRef = zeroName.copy();
        zeroRef.args.clear();
        for (Node arg : decl.ref.args) {
            zeroRef.args.add(arg.copy());
        }

        PullInfo tmp = pull(decl.rhs.copy(), symArg);

        RuleDecl oneDecl = oneRef.makeRule();
        oneDecl.rhs = tmp.one;
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);
        declSet.add(oneDecl);

        if (tmp.zero != null) {
            RuleDecl zeroDecl = zeroRef.makeRule();
            zeroDecl.rhs = tmp.zero;
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            declSet.add(zeroDecl);
            //info.zero = zeroName;
        }
        return info;
    }


    //update sym if it conflicts another arg in ref
    private void update(Name ref, Name sym) {
        int count = 1;
        String name = sym.astInfo.factorName;
        if (name.equals("res")) {
            name = sym.astInfo.factorName + ++count;
        }
        for (Node arg : ref.args) {
            if (arg.astInfo.factorName.equals(name)) {
                name = sym.astInfo.factorName + ++count;
            }
        }
        sym.astInfo.factorName = name;
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
                Sequence s1 = new Sequence(ai.zero.copy(), B.copy());
                Sequence s2 = new Sequence(ai.one.copy(), B.copy());
                return pull(new Or(s1, s2), sym);
            }
            //(a A1 | A0) B
            //a A(a) B | A_no_a B
            if (ai.zero != null) {
                info.zero = new Sequence(ai.zero.copy(), B.copy());
                info.zero.astInfo = s.astInfo.copy();
            }
            info.one = new Sequence(ai.one.copy(), B.copy());
            info.one.astInfo = s.astInfo.copy();
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
            Node s2 = new Sequence(a1.eps.copy(), pb.one);
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
                info.zero = withAst(new Regex(pi.zero.copy(), "?"), regex);
            }
            else {
                info.zero = new Epsilon();
            }
            return info;
        }
        else if (regex.isPlus()) {
            //A+=A A*
            Node star = withAst(new Regex(regex.node.copy(), "*"), regex);
            if (regex.astInfo.isFactor) {
                star.astInfo.loopExtra = sym.astInfo;
            }
            Node pre = regex.node.copy();
            Sequence s = new Sequence(pre, star);
            return pull(s, sym);
        }
        else {
            //A* = A+ | €
            Node plus = withAst(new Regex(regex.node.copy(), "+"), regex);
            if (regex.astInfo.isFactor) {
                plus.astInfo.loopExtra = sym.astInfo;
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
