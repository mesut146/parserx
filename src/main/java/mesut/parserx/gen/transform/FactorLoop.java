package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

public class FactorLoop extends Transformer {
    static boolean debugMethod = false;
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
            }
            else {
                i++;
            }
        }
    }

    private void factorRule(RuleDecl decl) {
        decl.rhs = transformNode(decl.rhs, null);
    }

    public Name noLoop(Regex sym) {
        return sym.node.asName().copy();
    }

    public List<Name> commons(Set<Name> s1, Set<Name> s2) {
        Set<Name> common = new HashSet<>(s1);
        common.retainAll(s2);
        //rule has higher priority
        List<Name> res = new ArrayList<>();
        List<Name> tokens = new ArrayList<>();
        for (Name name : common) {
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

    boolean hasLoop(Node node, Name sym) {
        if (debugMethod) System.out.println("hasLoop node = " + node + ", sym = " + sym);

        if (!FirstSet.start(node, sym, tree)) return false;
        if (node.isRegex()) {
            if (node.astInfo.isFactored) return false;
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                if (regex.node.equals(sym)) {
                    return true;
                }
                else {
                    //A* -> a*
                    //follow of a can be empty
                    Node f = follow(regex.node, sym);
                    return FirstSet.canBeEmpty(f, tree);
                }
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return false;
            Name name = node.asName();
            if (name.isRule()) {
                //todo recursion
                return hasLoop(tree.getRule(name).rhs, sym);
            }
            return false;
        }
        else if (node.isGroup()) {
            return hasLoop(node.asGroup().node, sym);
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            Node a = seq.first();
            Node b = Helper.trim(seq);
            //return hasLoop(a, sym) || Helper.canBeEmpty(a, tree) && hasLoop(b, sym);
            if (hasLoop(a, sym)) {
                return true;
            }
            else {
                if (FirstSet.canBeEmpty(a, tree)) {
                    return hasLoop(b, sym);
                }
                else {
                    return false;
                }
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Node a = or.first();
            Node b = Helper.trim(or);
            return hasLoop(a, sym) || hasLoop(b, sym);
        }
        return false;
    }

    public Set<Name> loops(Node node) {
        Set<Name> res = new HashSet<>();
        if (node.isRegex()) {
            if (node.astInfo.isFactored) return null;
            Regex regex = node.asRegex();
            if (!regex.isOptional()) {
                res.add(regex.node.asName());
                for (Name s : FirstSet.firstSet(regex.node.asName(), tree)) {
                    if (FirstSet.isEmpty(follow(regex.node, s), tree)) {
                        res.add(s);
                    }
                }
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return res;
            Name name = node.asName();
            if (name.isRule()) {
                return loops(tree.getRule(name).rhs);
            }
        }
        else if (node.isGroup()) {
            return loops(node.asGroup().node);
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            Node a = seq.first();
            Node b = Helper.trim(seq);

            res = loops(a);
            if (res != null) {
                if (FirstSet.canBeEmpty(a, tree)) {
                    res.addAll(loops(b));
                }
                return res;
            }
            else {
                if (FirstSet.canBeEmpty(a, tree)) {
                    return loops(b);
                }
            }
        }
        else if (node.isOr()) {
            Or or = node.asOr();
            Node a = or.first();
            Node b = Helper.trim(or);
            res = loops(a);
            res.addAll(loops(b));
        }
        return res;
    }

    public Node follow(Node node, Name first) {
        if (debugMethod) System.out.println("follow node = " + node + ", first = " + first);
        //if (!Helper.start(node, first, tree)) return null;
        //if (node.astInfo.isFactored) return null;
        if (node.isSequence()) {
            Node A = node.asSequence().first();
            Node B = Helper.trim(node.asSequence());
            if (A.equals(first)) {
                return B;
            }
            //A B
            if (Helper.start(A, first, tree)) {
                Node fa = follow(A, first);
                if (Helper.canBeEmpty(A, tree) && Helper.start(B, first, tree)) {
                    //A_eps B | A_noe B
                    throw new RuntimeException("not yet");
                }
                if (fa.isEpsilon()) {
                    return B;
                }
                else {
                    return Sequence.make(fa, B);
                }
            }
            else {
                //A can be empty
                return follow(B, first);
            }
        }
        else if (node.isOr()) {
            Node A = node.asOr().first();
            Node B = Helper.trim(node.asOr());
            if (Helper.start(A, first, tree)) {
                Node fa = follow(A, first);
                if (Helper.start(B, first, tree)) {
                    return new Or(fa, follow(B, first));
                }
                else {
                    return fa;
                }
            }
            else {
                return follow(B, first);
            }
        }
        else if (node.isGroup()) {
            return follow(node.asGroup().node, first);
        }
        else if (node.isRegex()) {
            if (node.astInfo.isFactored) return null;
            Regex regex = node.asRegex();
            Node f = follow(regex.node, first);
            if (regex.isOptional()) {
                return f;
            }
            else if (regex.isPlus()) {
                //A+=A A*
                return new Sequence(f, new Regex(regex.node, "*"));
            }
            else {
                //A*=A A* | €
                return new Sequence(f, new Regex(regex.node, "*"));
            }
        }
        else if (node.isName()) {
            if (node.astInfo.isFactored) return null;
            if (node.equals(first)) {
                return new Epsilon();
            }
            Name name = node.asName();
            if (name.isToken) {
                throw new RuntimeException("invalid call to follow");
            }
            RuleDecl decl = tree.getRule(name);
            return follow(decl.rhs, first);
        }
        throw new RuntimeException("invalid node");
    }

    public commonResult commons(Node a, Node b) {
        Set<Name> s1 = factor.first(a);
        Set<Name> s2 = factor.first(b);
        Set<Name> common = new HashSet<>(s1);
        common.retainAll(s2);
        commonResult res = new commonResult();
        if (a.isName() && common.contains(a.asName())) {
            res.name = a.asName();
            return res;
        }
        if (b.isName() && common.contains(b.asName())) {
            res.name = b.asName();
            return res;
        }
        if (a.isRegex() && a.asRegex().asName().equals(b)) {
            //a+ a
            res.isLoop = true;
            res.name = b.asName();
            return res;
        }
        if (b.isRegex() && b.asRegex().asName().equals(a)) {
            //a a+());
            res.isLoop = true;
            res.name = a.asName();
            return res;
        }
        List<Name> list = new ArrayList<>(common);
        //rule has higher priority
        Collections.sort(list, new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                if (o1.isRule()) {
                    return o2.isRule() ? 0 : -1;
                }
                else {
                    return 1;
                }
            }
        });

        for (Name name : list) {
            if (name.isToken) break;
            if (hasLoop(a, name) && hasLoop(b, name)) {
                if (name.isRule()) {
                    res.isLoop = true;
                    res.name = name;
                    return res;
                }
            }
        }
        //no rule is common try tokens
        for (Name name : list) {
            if (name.isToken) {
                if (hasLoop(a, name) && hasLoop(b, name)) {
                    res.isLoop = true;
                    res.name = name;
                    return res;
                }
                else {
                    res.name = name;
                }
            }
        }
        if (res.name != null) {
            //regular token
            return res;
        }
        return null;
    }

    @Override
    public Node visitSequence(Sequence s, Void arg) {
        factor.curRule = curRule;
        return factor.visitSequence(s, arg);
    }

    @Override
    public Node visitOr(Or or, Void arg) {
        Node node = super.visitOr(or, arg);
        if (!node.isOr()) return node;
        or = node.asOr();

        for (int i = 0; i < or.size(); i++) {
            for (int j = i + 1; j < or.size(); j++) {
                commonResult result = commons(or.get(i), or.get(j));
                if (result == null) continue;
                //try each sym
                Name sym = result.name.copy();
                sym.astInfo = new AstInfo();
                if (result.isLoop) {
                    Regex factorSym = new Regex(sym, "+");
                    if (Factor.debug) System.out.printf("factoring loop %s in %s\n", factorSym, curRule.ref);
                    modified = true;
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
                    Node res = factor.visitOr(or, arg);
                    modified = factor.modified;
                    return res;
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
            return cache.get(key);
        }

        RuleDecl decl = tree.getRule(name);
        Factor.PullInfo info = pull(decl.rhs, sym);
        if (info == null) {
            cache.put(key, null);
            return null;
        }
        cache.put(key, info);

        Name oneName = new Name(tree.getName(factor.getBase(name).name));
        oneName.args = new ArrayList<>(name.args);
        oneName.args.add(sym);
        oneName.astInfo = name.astInfo.copy();
        RuleDecl oneDecl = oneName.makeRule();
        oneDecl.rhs = info.one;
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);
        factor.senderMap.put(oneName, factor.senderMap.get(name));
        if (info.zero != null) {
            Name zeroName;
            if (sym.isStar()) {
                zeroName = new Name(name.name + "_nos_" + noLoop(sym));
            }
            else {
                zeroName = new Name(name.name + "_nop_" + noLoop(sym));
            }
            zeroName.args = new ArrayList<>(name.args);
            zeroName.astInfo = name.astInfo.copy();
            RuleDecl zeroDecl = zeroName.makeRule();
            zeroDecl.rhs = info.zero;
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            factor.senderMap.put(zeroName, factor.senderMap.get(name));
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
