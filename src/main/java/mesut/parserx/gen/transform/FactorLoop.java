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
                FactorHelper.commonResult result = helper.commons(or.get(i), or.get(j));
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
        if (!helper.hasLoop(node, normal)) {
            throw new RuntimeException("can't pull " + sym + " from " + node);
        }

        if (!Helper.first(node, tree, true).contains(normal)) {
            return null;
        }
        if (sym.node.astInfo.varName == null) {
            factor.curRule = curRule;
            sym.astInfo.isFactor = true;
            sym.astInfo.varName = factor.factorName(normal);
            sym.node.astInfo.varName = sym.astInfo.varName;
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
            if (helper.hasLoop(ch, noLoop(sym))) {
                Factor.PullInfo tmp = pull(ch, sym);
                one.add(tmp.one);
                if (tmp.zero != null) {
                    zero.add(tmp.zero);
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
        Node A = seq.first();
        Node B = Helper.trim(seq);
        Factor.PullInfo res = new Factor.PullInfo();

        if (helper.hasLoop(A, noLoop(sym))) {
            if (FirstSet.isEmpty(A, tree) && helper.hasLoop(B, noLoop(sym))) {
                //A (a+ B(a+) | B_nop_a) = A a+ B(a+) | A B_nop_a
                Factor.PullInfo bi = pull(B, sym);
                Sequence one = new Sequence(A, bi.one);
                Sequence zero = new Sequence(A, bi.zero);
                res.one = one;
                res.zero = zero;
                return res;
            }
            else if (FirstSet.canBeEmpty(A, tree) && helper.hasLoop(B, noLoop(sym))) {
                //A_eps B | A_noe B
                Epsilons.Info eps = Epsilons.trimInfo(A, tree);
                Sequence s1 = new Sequence(eps.eps, B);
                Sequence s2 = new Sequence(eps.noEps, B);
                return pull(new Or(s1, s2), sym);
            }
            else {
                //a+ A(a+) B | A_nop_a B
                Factor.PullInfo ai = pull(A, sym);
                res.one = Sequence.make(ai.one, B.copy());
                res.one.astInfo = seq.astInfo.copy();
                if (ai.zero != null) {
                    res.zero = Sequence.make(ai.zero, B.copy());
                    res.zero.astInfo = seq.astInfo.copy();
                }
                return res;
            }
        }
        else {
            //A can be empty B starts
            //A_noe B | A_eps B
            //A_noe B | a+ A_eps B(a+) | A_eps B_nop_a
            Factor.PullInfo bi = pull(B, sym);
            Epsilons.Info eps = Epsilons.trimInfo(A, tree);
            res.one = Sequence.make(eps.eps, bi.one);
            Sequence s2 = bi.zero == null ? null : Sequence.make(eps.eps, bi.zero);
            if (eps.noEps == null) {
                res.zero = s2;
                return res;
            }
            Sequence s1 = Sequence.make(eps.noEps, B);
            res.one = s2 == null ? s1 : new Or(s1, s2);
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
        cache.put(key, info);

        Name oneName = tree.getFactorPlusOne(name, sym);
        RuleDecl oneDecl = oneName.makeRule();
        oneDecl.rhs = info.one;
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);

        //todo oneref

        if (info.zero != null) {
            Name zeroName = tree.getFactorPlusZero(name, sym);
            RuleDecl zeroDecl = zeroName.makeRule();
            zeroDecl.rhs = info.zero;
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            info.zero = zeroName;
        }

        info.one = oneName;
        return info;
    }

    Factor.PullInfo pullStar(Regex regex, Regex sym) {
        Factor.PullInfo res = new Factor.PullInfo();
        if (regex.node.equals(noLoop(sym))) {
            //base case
            //A*: A+ | €
            Regex copy = regex.copy();
            copy.astInfo.isFactored = true;
            copy.astInfo.factor = sym.astInfo;
            res.one = copy;
            res.zero = new Epsilon();
            return res;
        }
        //A*: A+ | €
        Regex plus = new Regex(regex.node.copy(), "+");
        plus.astInfo = regex.astInfo.copy();
        Factor.PullInfo tmp = pull(plus, sym);
        if (tmp.zero == null) {
            res.zero = new Epsilon();
        }
        else {
            res.zero = new Or(tmp.zero, new Epsilon());
        }
        return res;
    }

    Factor.PullInfo pullPlus(Regex regex, Regex sym) {
        Factor.PullInfo res = new Factor.PullInfo();
        if (regex.node.equals(noLoop(sym))) {
            //base case
            Regex copy = regex.copy();
            copy.astInfo.isFactored = true;
            copy.astInfo.factor = sym.astInfo;
            res.one = copy;
            return res;
        }
        if (helper.hasLoop(regex.node, noLoop(sym))) {
            //A+ = A A*
            Regex star = new Regex(regex.node.copy(), "*");
            star.astInfo = regex.astInfo.copy();
            return pull(new Sequence(regex.node.copy(), star), sym);
        }
        else {
            Name factorSingle = noLoop(sym);
            factor.curRule = curRule;
            factorSingle.astInfo.isFactor = true;
            factorSingle.astInfo.varName = factor.factorName(factorSingle);
            Factor.PullInfo tmp = factor.pull(regex.node, factorSingle);
            if (FirstSet.isEmpty(tmp.one, tree)) {
                Regex factored = new Regex(tmp.one.copy(), "+");
                factored.astInfo = regex.astInfo.copy();
                factored.astInfo.isFactored = true;
                factored.astInfo.factor = sym.astInfo;
                if (tmp.zero == null) {
                    //(a A(a))+
                    //a+ A(a)+
                    res.one = factored;
                    return res;
                }
                else {
                    //(a A(a) | A_no_a)+
                    //(a A(a))+ (A_no_a A* | €) | A_no_a A*
                    //a+ (A(a)()+ A_no_a A* | A(a)()+) | A_no_a A*
                    Regex star = new Regex(regex.node, "*");
                    star.astInfo = regex.astInfo.copy();
                    Sequence s = new Sequence(factored, tmp.zero, star);
                    res.one = new Or(s, factored);
                    res.zero = new Sequence(tmp.zero, star);
                    return res;
                }
            }
            else if (FirstSet.canBeEmpty(tmp.one, tree)) {
                if (tmp.zero == null) {
                    //A+ = (a A(a))+ = (a A_a_eps(a) | a A_a_noe(a))+
                    //(a A_a_eps(a))+ (a A_a_noe(a) A*)? | a A_a_noe(a) A*
                    //a+ A_a_eps(a)+ (a A_a_noe(a) A*)? | a A_a_noe(a) A*
                    Epsilons.Info eps = Epsilons.trimInfo(tmp.one, tree);

                    Regex factored = new Regex(eps.eps.copy(), "+");
                    factored.astInfo = regex.astInfo.copy();
                    factored.astInfo.isFactored = true;
                    factored.astInfo.factor = sym.astInfo;

                    Regex star = new Regex(regex.node, "*");
                    star.astInfo = regex.astInfo.copy();

                    Name factor = noLoop(sym);
                    factor.astInfo.isFactor = true;
                    Sequence tail = new Sequence(factorSingle.copy(), eps.noEps.copy(), star.copy());
                    Sequence one = new Sequence(factored, new Regex(new Group(tail), "?"));
                    Sequence zero = new Sequence(factorSingle.copy(), eps.noEps.copy(), star.copy());
                    res.one = one;
                    res.zero = zero;
                    return res;
                }
                else {
                    //A+ = (a A(a) | A_no_a)+ = (a A_a_eps(a) | a A_a_noe(a) | A_no_a)+
                    //(a A_a_eps(a))+ (a A_a_noe(a) A* | A_no_a A*)? | a A_a_noe(a) A* | A_no_a A*
                    //a+ A_a_eps(a)+ (a A_a_noe(a) A* | A_no_a A*)? | a A_a_noe(a) A* | A_no_a A*
                    throw new RuntimeException("not yet");
                }
            }
            else {
                throw new RuntimeException("not possible");
            }
        }
    }

    Factor.PullInfo pullRegex(Regex regex, Regex sym) {
        if (regex.isStar()) {
            return pullStar(regex, sym);
        }
        else if (regex.isPlus()) {
            return pullPlus(regex, sym);
        }
        else {
            //A? = A | € = a+ A(a+) | A_nop_a | €
            Factor.PullInfo res = new Factor.PullInfo();
            Factor.PullInfo tmp = pull(regex.node, sym);
            res.one = tmp.one;
            res.zero = new Regex(tmp.zero, "?");
            return res;
        }
    }


}
