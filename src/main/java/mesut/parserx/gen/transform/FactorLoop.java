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

    public FactorLoop(Tree tree) {
        super(tree);
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
                        if (debug)
                            System.out.printf("factoring %s in %s\n", regex, curRule.reff);

                        modified = true;
                        Factor.PullInfo info = pullOr(or, regex);
                        Group g = new Group(info.one);
                        g.astInfo.isFactorGroup = true;
                        Node one = Sequence.of(regex, g);
                        if (info.zero == null) {
                            return one;
                        }
                        else {
                            return new Or(one, info.zero).normal();
                        }
                    }
                }
            }
        }
        return or;
    }

    public Factor.PullInfo pull(Node node, Regex sym) {
        if (!sym.isStar() && !sym.isPlus()) {
            throw new RuntimeException("loop sym expected");
        }
        Name normal = noLoop(sym);
        if (!Helper.first(node, tree, true).contains(normal)) {
            return null;
            //throw new RuntimeException("can't pull " + sym + " from " + node);
        }
        Factor.PullInfo info = new Factor.PullInfo();
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
            else {
                info = null;
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
        res.one = new Or(one).normal();
        if (!zero.isEmpty()) {
            res.zero = new Or(zero).normal();
        }
        return res;
    }

    Factor.PullInfo pullSeq(Sequence seq, Regex sym) {
        Node a = seq.first();
        Node b = Helper.trim(seq);

        /*if (Helper.canBeEmpty(a, tree)) {
            //A_noe B | B
            return pull(new Or(new Sequence(new Epsilons(tree).trim(a), b), b), sym);
        }*/

        Factor.PullInfo info = pull(a, sym);
        if (info == null) {
            if (Helper.canBeEmpty(a, tree)) {
                //todo a can be empty b starts
                info = pull(b, sym);
                if (info != null) {
                    //A_noe B | B = A_noe B | a* B_no_a B | B_no_a
                    Factor.PullInfo res = new Factor.PullInfo();
                    res.zero = new Sequence(Epsilons.trim(a, tree), b);
                    //res.one=
                    return res;
                }
            }
            //or multiple factor
            return null;
        }
        else {
            Factor.PullInfo res = new Factor.PullInfo();
            res.one = new Sequence(info.one, b).normal();
            if (info.zero != null) {
                res.zero = new Sequence(info.zero, b).normal();
            }
            if (Helper.canBeEmpty(a, tree)) {
                //todo b starts
                /*if (res.zero == null) {
                    res.zero = b;
                }
                else {
                    res.zero = new Or(res.zero, b);
                }*/
            }
            return res;
        }
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
                Factor.PullInfo res = new Factor.PullInfo();
                Regex copy = sym.copy();
                copy.astInfo.isFactored = true;
                res.one = copy;
                if (sym.isPlus()) {
                    //A*: A+ | €
                    res.zero = new Epsilon();
                }
                return res;
            }
            if (sym.isPlus()) {
                //A*: A+ | €
                Regex plus = new Regex(regex.node, "+");
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
            Name no = noLoop(sym);
            Factor.PullInfo tmp = new Factor(tree).pull(regex.node, no);
            if (tmp.one == null) {
                return null;
            }
            if (tmp.zero == null) {
                //A must start multiple a
                throw new RuntimeException("not supported yet");
            }
            //A* = (a A(a) | A_no_a)*
            if (isEpsilon(tmp.one)) {
                //(a A(a))* (A_no_a A* | €)
                //(a A(a))* A_no_a A* | Ano_a A* | €
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Or(new Sequence(tmp.zero, regex.copy()), new Epsilon());
                //res.zero = new Sequence(tmp.zero, regex.copy());
                return res;
            }
            else if (Helper.canBeEmpty(tmp.one, tree)) {
                throw new RuntimeException("A(a) not epsilon");
                //A* = (a A_a_noe(a) | a | A_no_a)*
                //A* = a* (a A_a_noe(a) | A_no_a) A* | A_no_a A*
                //todo factor greedy
                /*Node trimmed = new Epsilons(tree).trim(tmp.one);
                if (Helper.start(trimmed, sym, tree)) {
                    throw new RuntimeException("consecutive factors");
                }
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Sequence(new Group(new Or(new Sequence(sym, trimmed), tmp.zero)), regex.copy());
                res.zero = new Sequence(tmp.zero, regex.copy());
                return res;*/
            }
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
            if (isEpsilon(tmp.one)) {
                //(a A(a))+ (A_no_a A* | €) | A_no_a A*
                Factor.PullInfo res = new Factor.PullInfo();
                res.one = new Or(new Sequence(tmp.zero, star), new Epsilon());
                res.zero = new Sequence(tmp.zero, star);
                return res;
            }
            if (Helper.canBeEmpty(tmp.one, tree)) {
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
