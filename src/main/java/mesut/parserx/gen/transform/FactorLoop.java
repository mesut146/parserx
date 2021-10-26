package mesut.parserx.gen.transform;

import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

public class FactorLoop extends SimpleTransformer {
    public static boolean keepFactor = true;
    public static boolean debug = false;
    boolean modified;
    RuleDecl curRule;
    boolean any;
    Map<String, Factor.PullInfo> cache = new HashMap<>();
    Factor factor;

    public FactorLoop(Tree tree) {
        super(tree);
        factor = new Factor(tree);
    }

    public static List<Name> commons(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        //rule has higher priority
        List<Name> res = new ArrayList<>();
        List<Name> tokens = new ArrayList<>();
        for (Name name : copy) {
            if (!name.isEpsilon()) {
                if (name.isRule()) {
                    res.add(name);
                }
                else {
                    tokens.add(name);
                }
            }
        }
        res.addAll(tokens);
        if (res.isEmpty()) {
            return null;
        }
        return res;
    }

    public static Name noLoop(Regex sym) {
        return sym.node.asName().copy();
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
        decl.rhs = transformNode(decl.rhs, decl);
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
                List<Name> syms = commons(s1, s2);
                if (syms == null) continue;
                //try each sym
                for (Name sym : syms) {
                    Regex regex = new Regex(sym.copy(), "+");//todo what about star?
                    Factor.PullInfo info1 = pull(or.get(i), regex);
                    Factor.PullInfo info2 = pull(or.get(j), regex);
                    if (info1 != null && info2 != null) {
                        if (debug) System.out.printf("factoring %s in %s\n", regex, curRule.ref);
                        modified = true;
                        regex.astInfo.isFactor = true;
                        Factor.PullInfo info = pullOr(or, regex);
                        Group g = new Group(info.one);
                        Node one = Sequence.of(regex, g);
                        if (info.zero == null) {
                            return one;
                        }
                        else {
                            return new Or(one, info.zero);
                        }
                    }
                }
            }
        }
        return or;
    }

    public Factor.PullInfo pull(Node node, Regex sym) {
        if (sym.isStar()) {
            throw new RuntimeException("deprecated");
        }
        if (!sym.isStar() && !sym.isPlus()) {
            throw new RuntimeException("loop sym expected");
        }
        Name normal = noLoop(sym);
        if (!Helper.first(node, tree, true).contains(normal)) {
            return null;
        }
        if (sym.astInfo.factorName == null) {
            sym.astInfo.isFactor = true;
            sym.astInfo.factorName = factor.factorName(normal);
        }
        Factor.PullInfo info = new Factor.PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.isRule()) {
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

    Factor.PullInfo pullOr(Or or, Regex sym) {
        List<Node> one = new ArrayList<>();
        List<Node> zero = new ArrayList<>();
        for (Node ch : or) {
            Factor.PullInfo info = pull(ch, sym);
            if (info != null) {
                one.add(info.one);
                if (info.zero != null) {
                    zero.add(info.zero);
                }
            }
            else {
                zero.add(ch);
            }
        }
        if (one.isEmpty()) {
            return null;
        }
        Factor.PullInfo res = new Factor.PullInfo();
        res.one = new Or(one);
        if (!zero.isEmpty()) {
            if (zero.size() == 1) {
                res.zero = zero.get(0);
            }
            else {
                res.zero = new Or(zero);
            }
        }
        return res;
    }

    Factor.PullInfo pullSeq(Sequence seq, Regex sym) {
        Node a = seq.first();
        Node b = Helper.trim(seq);

        Factor.PullInfo info = pull(a, sym);
        if (info == null) {
            if (Helper.canBeEmpty(a, tree)) {
                //todo a can be empty b starts
                info = pull(b, sym);
                if (info != null) {
                    //A_noe B | A_eps B = A_noe B | a+ A_eps B(a+) | A_eps B_nop_a
                    Epsilons.Info eps = Epsilons.trimInfo(a, tree);
                    Factor.PullInfo res = new Factor.PullInfo();
                    res.one = new Sequence(eps.eps, info.one);
                    Sequence s1 = new Sequence(eps.noEps, b);
                    Sequence s2 = new Sequence(eps.eps, info.zero);
                    res.one = new Or(s1, s2);
                    return res;
                }
            }
            return null;
        }
        else {
            Factor.PullInfo res = new Factor.PullInfo();
            res.one = new Sequence(wrap(info.one), b);
            res.one.astInfo = seq.astInfo.copy();
            if (info.zero != null) {
                res.zero = new Sequence(wrap(info.zero), b);
                res.zero.astInfo = seq.astInfo.copy();
            }
            if (Helper.canBeEmpty(a, tree)) {
                //A_eps B | A_noe B
                Factor.PullInfo bi = pull(b, sym);
                if (bi != null) {
                    throw new RuntimeException("not yet");
                    /*Epsilons.Info eps = Epsilons.trimInfo(a, tree);
                    Sequence s1 = new Sequence(eps.noEps, b);
                    Sequence s2 = new Sequence(eps.eps, b);
                    return pullOr(new Or(s1, s2), sym);*/
                }
                else {
                    return res;
                }
            }
            return res;
        }
    }

    Node wrap(Node node) {
        if (node.isOr()) {
            return new Group(node);
        }
        return node;
    }

    Factor.PullInfo pullRule(Name name, Regex sym) {
        String key = name + "-" + sym;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Factor.PullInfo info = new Factor.PullInfo();
        cache.put(key, info);
        RuleDecl decl = tree.getRule(name);
        info = pull(decl.rhs, sym);
        if (info == null || info.one == null) {
            cache.put(key, null);
            return null;
        }

        Name oneSym = new Name(name.name);
        oneSym.args.add(sym);
        RuleDecl oneDecl = oneSym.makeRule();
        oneDecl.rhs = info.one;
        tree.addRule(oneDecl);

        Name zeroSym;
        if (sym.isStar()) {
            zeroSym = new Name(name.name + "_nos_" + noLoop(sym));
        }
        else {
            zeroSym = new Name(name.name + "_nop_" + noLoop(sym));
        }
        RuleDecl zeroDecl = zeroSym.makeRule();
        zeroDecl.rhs = info.zero;
        tree.addRule(zeroDecl);

        info.one = oneSym;
        info.zero = zeroSym;
        return info;
    }

    Factor.PullInfo pullRegex(Regex regex, Regex sym) {
        if (regex.isStar()) {
            if (regex.node.equals(noLoop(sym))) {
                //base case
                //A*: A+ | €
                Factor.PullInfo res = new Factor.PullInfo();
                Regex copy = sym.copy();
                copy.astInfo.isFactored = true;
                res.one = copy;
                res.zero = new Epsilon();
                return res;
            }
            //A*: A+ | €
            Regex plus = new Regex(regex.node, "+");
            plus.astInfo = regex.astInfo.copy();
            Factor.PullInfo info = pull(plus, sym);
            if (info == null) return null;
            if (info.zero == null) {
                info.zero = new Epsilon();
            }
            else {
                info.zero = new Or(info.zero, new Epsilon());
            }
            return info;
        }
        else if (regex.isPlus()) {
            if (sym.isStar()) {
                throw new RuntimeException("can't pull star from plus");
            }
            if (regex.node.equals(noLoop(sym))) {
                //base case
                Factor.PullInfo res = new Factor.PullInfo();
                Regex copy = sym.copy();
                copy.astInfo.isFactored = true;
                res.one = copy;
                return res;
            }
            Factor.PullInfo tmp = new Factor(tree).pull(regex.node, noLoop(sym));
            Regex star = new Regex(regex.node, "*");
            star.astInfo = regex.astInfo.copy();
            if (isEpsilon(tmp.one)) {
                //(a A(a) | A_no_a)+
                //(a A(a))+ (A_no_a A* | €) | A_no_a A*
                Factor.PullInfo res = new Factor.PullInfo();
                Sequence s = new Sequence(tmp.zero, star);
                res.one = new Or(s, new Epsilon());
                res.zero = s;
                return res;
            }
            if (Helper.canBeEmpty(tmp.one, tree)) {
                throw new RuntimeException("not yet");
                //A+: a+ (a+(a+) | a+(a+) A_a_noe(a) A* | a+(a+) A_no_a A*) | A_no_a A*
                /*Factor.PullInfo res = new Factor.PullInfo();
                res.zero = new Sequence(tmp.zero, star);
                res.one = new Or();
                return res;*/
            }
        }
        else {
            //A?
            Factor.PullInfo info = pull(regex.node, sym);
            if (info != null) {
                //A?: A | € = a* A1 | €
                info.zero = new Regex(info.zero, "?");
                return info;
            }
        }
        return null;
    }

    boolean isEpsilon(Node node) {
        Set<Name> set = Helper.first(node, tree, true);
        return set.isEmpty() || (set.size() == 1 && set.iterator().next().equals(node));
    }

}
