package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactorLoop extends Transformer {
    static boolean debugMethod = false;
    public FactorHelper helper;
    boolean modified;
    RuleDecl curRule;
    boolean any;
    Map<String, Factor.PullInfo> cache = new HashMap<>();
    Factor factor;

    public FactorLoop(Tree tree, Factor factor) {
        super(tree);
        if (factor == null) {
            factor = new Factor(tree);
        }
        this.factor = factor;
        helper = new FactorHelper(tree, factor);
    }

    public void factorize() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i);
            factorRule(decl);
            i++;
        }
    }

    public void factorRule(RuleDecl decl) {
        curRule = decl;
        modified = false;
        decl.rhs = transformNode(decl.rhs, null);
        if (modified) {
            any = true;
            factorRule(decl);
        }
    }

    public Name noLoop(Regex sym) {
        return sym.node.asName().copy();
    }


    /*@Override
    public Node visitSequence(Sequence s, Void arg) {
        factor.curRule = curRule;
        return factor.visitSequence(s, arg);
    }*/

    @Override
    public Node visitOr(Or or, Void arg) {
        Node node = super.visitOr(or, arg);
        if (!node.isOr()) return node;
        or = node.asOr();

        for (int i = 0; i < or.size(); i++) {
            for (int j = i + 1; j < or.size(); j++) {
                commonResult result = helper.commons(or.get(i), or.get(j));
                if (result == null) continue;
                modified = true;

                Name sym = result.name.copy();
                sym.astInfo = new AstInfo();
                if (result.isLoop) {
                    Regex factorSym = new Regex(sym, "+");
                    if (Factor.debug) System.out.printf("factoring loop %s in %s\n", factorSym, curRule.ref);
                    factorSym.astInfo.isFactor = true;
                    factorSym.node.astInfo.isFactor = true;
                    Factor.PullInfo info = pull(or, factorSym);
                    Node one = new Sequence(factorSym, new Group(info.one));
                    if (info.zero == null) {
                        return one;
                    }
                    else {
                        return Or.make(one, info.zero);
                    }
                }
                else {
                    //single factor
                    factor.curRule = curRule;
                    return factor.factorOr(or, sym);
                }
            }
        }
        return or;
    }

    public Factor.PullInfo pull(Node node, Regex sym) {
        if (sym.isStar()) {
            throw new RuntimeException("deprecated");
        }
        if (!sym.isPlus()) {
            throw new RuntimeException("loop sym expected");
        }
        Name normal = noLoop(sym);
        if (!Helper.first(node, tree, true).contains(normal)) {
            return null;
        }
        if (sym.astInfo.factorName == null) {
            sym.astInfo.isFactor = true;
            factor.curRule = curRule;
            sym.astInfo.factorName = factor.factorName(normal);
        }
        if (node.isName()) {
            Name name = node.asName();
            if (name.isRule()) {
                return pullRule(name, sym);
            }
            else {
                return null;
            }
        }
        else if (node.isGroup()) {
            return pull(node.asGroup().node, sym);
        }
        else if (node.isOr()) {
            return pullOr(node.asOr(), sym);
        }
        else if (node.isSequence()) {
            return pullSeq(node.asSequence(), sym);
        }
        else if (node.isRegex()) {
            return pullRegex(node.asRegex(), sym);
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass());
        }
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
        Factor.PullInfo res = new Factor.PullInfo();
        if (one.size() == 1) {
            res.one = one.get(0);
        }
        else {
            res.one = new Or(one);
        }
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
            //a can be empty b starts
            if (Helper.canBeEmpty(a, tree)) {
                //todo a can be empty b starts
                info = pull(b, sym);
                if (info != null) {
                    //A_noe B | A_eps B = A_noe B | a+ A_eps B(a+) | A_eps B_nop_a
                    Epsilons.Info eps = Epsilons.trimInfo(a, tree);
                    Factor.PullInfo res = new Factor.PullInfo();
                    res.one = Sequence.make(eps.eps, info.one);
                    Sequence s2 = info.zero == null ? null : Sequence.make(eps.eps, info.zero);
                    if (eps.noEps == null) {
                        res.zero = s2;
                        return res;
                    }
                    Sequence s1 = Sequence.make(eps.noEps, b);
                    res.one = s2 == null ? s1 : new Or(s1, s2);
                    return res;
                }
            }
            return null;
        }
        else {
            Factor.PullInfo res = new Factor.PullInfo();
            res.one = Sequence.make(info.one, b);
            res.one.astInfo = seq.astInfo.copy();
            if (info.zero != null) {
                res.zero = Sequence.make(info.zero, b);
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

    Factor.PullInfo pullRule(Name name, Regex sym) {
        if (Factor.debugPull) {
            System.out.println("pullRule2 " + name + " " + sym);
        }
        String key = name.name + "-" + sym;
        if (cache.containsKey(key)) {
            Factor.PullInfo info = cache.get(key);
            info.one = info.one.copy();
            info.one.astInfo = name.astInfo.copy();
            if (info.zero != null) {
                info.zero = info.zero.copy();
                info.zero.astInfo = name.astInfo.copy();
            }
            return info;
        }

        RuleDecl decl = tree.getRule(name);
        Factor.PullInfo info = pull(decl.rhs, sym);
        if (info == null) {
            cache.put(key, null);
            return null;
        }
        cache.put(key, info);

        Name oneName = tree.getFactorPlusOne(name, noLoop(sym));
        RuleDecl oneDecl = oneName.makeRule();
        oneDecl.rhs = info.one;
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);

        //todo oneref

        if (info.zero != null) {
            Name zeroName = tree.getFactorPlusZero(name, noLoop(sym));
            RuleDecl zeroDecl = zeroName.makeRule();
            zeroDecl.rhs = info.zero;
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            info.zero = zeroName;
        }

        info.one = oneName;
        return info;
    }

    Factor.PullInfo pullRegex(Regex regex, Regex sym) {
        if (regex.isStar()) {
            if (regex.node.equals(noLoop(sym))) {
                //base case
                //A*: A+ | €
                Factor.PullInfo res = new Factor.PullInfo();
                Regex copy = regex.copy();
                copy.astInfo.isFactored = true;
                copy.astInfo.factorName = sym.astInfo.factorName;
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
                Regex copy = regex.copy();
                copy.astInfo.isFactored = true;
                copy.astInfo.factorName = sym.astInfo.factorName;
                res.one = copy;
                return res;
            }
            Factor.PullInfo tmp = factor.pull(regex.node, noLoop(sym));
            Regex star = new Regex(regex.node, "*");
            star.astInfo = regex.astInfo.copy();
            if (FirstSet.isEmpty(tmp.one, tree)) {
                Factor.PullInfo res = new Factor.PullInfo();
                if (tmp.zero == null) {
                    //(a A(a))+
                    //a+ A(a)+
                    res.one = new Regex(tmp.one, "+");
                    res.one.astInfo = regex.astInfo.copy();
                    res.one.astInfo.isFactored = true;
                    res.one.astInfo.factor = sym.astInfo;
                }
                else {
                    //(a A(a) | A_no_a)+
                    //(a A(a))+ (A_no_a A* | €) | A_no_a A*
                    //a+ (A(a)()+ A_no_a A* | A(a)()+) | A_no_a A*
                    Regex r = new Regex(tmp.one, "+");
                    r.astInfo = regex.astInfo.copy();
                    r.astInfo.isFactored = true;
                    r.astInfo.factor = sym.astInfo;
                    Sequence s = new Sequence(r, tmp.zero, star);
                    res.one = new Or(s, r);
                    res.zero = new Sequence(tmp.zero, star);
                }
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

    static class commonResult {
        boolean isLoop;
        Name name;
    }

}
