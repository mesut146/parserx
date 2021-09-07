package mesut.parserx.gen;

import mesut.parserx.gen.ll.AstInfo;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Factor extends SimpleTransformer {

    public static boolean keepFactor = true;
    HashMap<Name, PullInfo> cache = new HashMap<>();
    boolean modified;
    RuleDecl curRule;
    Map<Name, Integer> factorCount = new HashMap<>();

    public Factor(Tree tree) {
        super(tree);
    }

    public static Name encode(Name name) {
        boolean t = Name.debug;
        Name.debug = false;
        Name res = new Name(name.name + "_" + NodeList.join(name.args, "_"), false);
        res.args = new ArrayList<>(name.args);
        Name.debug = t;
        return res;
    }

    //return common sym in two list with all instances
    public static List<Name> common(List<Name> s1, List<Name> s2) {
        List<Name> common1 = new ArrayList<>();
        List<Name> common2 = new ArrayList<>();
        for (Name n1 : s1) {
            for (Name n2 : s2) {
                if (n1.equals(n2)) {
                    common1.add(n1);
                    common2.add(n2);
                }
            }
        }
        //find rule
        List<Name> res = new ArrayList<>();
        List<Name> res2 = new ArrayList<>();
        for (int i = 0; i < common1.size(); i++) {
            Name n = common1.get(i);
            if (!n.isEpsilon()) {
                //collect same symbols
                for (Name n1 : common1) {
                    if (n1.equals(n)) {
                        if (n.isRule()) {
                            res.add(n1);
                        }
                        else {
                            res2.add(n1);
                        }
                    }
                }
                for (Name n2 : common2) {
                    if (n2.equals(n)) {
                        if (n.isRule()) {
                            res.add(n2);
                        }
                        else {
                            res2.add(n2);
                        }
                    }
                }
                if (!res.isEmpty()) {
                    return res;
                }
                if (!res2.isEmpty()) {
                    return res2;
                }
            }
        }
        return null;
    }

    String factorName(Name sym) {
        if (factorCount.containsKey(sym)) {
            int i = factorCount.get(sym);
            i++;
            factorCount.put(sym, i);
            return sym + "f" + i;
        }
        else {
            factorCount.put(sym, 1);
            return sym + "f1";
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
            List<Name> s1 = Helper.firstList(or.get(i), tree);
            for (int j = i + 1; j < or.size(); j++) {
                List<Name> s2 = Helper.firstList(or.get(j), tree);
                List<Name> symList = common(s1, s2);
                if (symList != null) {
                    Name sym = symList.get(i);
                    //AstInfo astInfo = sym.astInfo.copy();//todo why?
                    sym = sym.copy();
                    sym.astInfo.isFactor = true;
                    sym.astInfo.factorName = factorName(sym);
                    /*for (Name sym0 : symList) {
                        //sym0.astInfo = astInfo;
                        sym0.astInfo.isFactored = true;//may be not all
                        sym0.astInfo.factorName = factorName(sym);
                    }*/
                    //sym.astInfo.code = null;
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
        }
        return or;
    }

    //factor sequence
    @Override
    public Node transformSequence(Sequence s, Node parent) {
        Node node = super.transformSequence(s, parent);
        if (node.isSequence()) {
            s = node.asSequence();
        }
        else {
            return node;
        }
        //A B needs factoring if A can be empty
        //A B -> A_no_eps B | A(A) B
        Node A = s.first();
        if (Helper.canBeEmpty(A, tree)) {
            Node B = Helper.trim(s);
            List<Name> s1 = Helper.firstList(A, tree);
            List<Name> s2 = Helper.firstList(B, tree);
            if (common(s1, s2) != null) {
                Node trimmed = new Epsilons(tree).trim(A);
                return transformOr(new Or(Sequence.of(trimmed, B), B), parent);
            }
        }
        return s;
    }

    public void factorize() {
        for (int i = 0; i < tree.rules.size(); ) {
            RuleDecl decl = tree.rules.get(i);
            curRule = decl;
            modified = false;
            factorRule(decl);
            if (modified) {
                //restart if any modification happens
                i = 0;
            }
            else {
                i++;
            }
        }
    }

    private void factorRule(RuleDecl decl) {
        decl.rhs = transformNode(decl.rhs, decl);
    }

    //pull a single token 'sym' and store result in info
    //A: sym A1 | A0
    //A0=part doesn't start with sym
    //A1=part after sym
    public PullInfo pull(Node node, Name sym) {
        if (sym.astInfo.isFactored) {
            throw new RuntimeException("factored sym");
        }
        //System.out.println("pull:" + node + " sym:" + sym);
        if (!Helper.first(node, tree, true).contains(sym)) {
            throw new RuntimeException("can't pull " + sym + " from " + node);
        }
        PullInfo info = new PullInfo();
        if (node.isName()) {
            Name name = node.asName();
            if (name.equals(sym)) {
                if (keepFactor) {
                    AstInfo astInfo = name.astInfo.copy();
                    name = name.copy();
                    name.astInfo = astInfo;
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
                zeroName.astInfo = name.astInfo.copy();

                Name oneName = name.copy();
                oneName.args.add(sym);
                oneName.astInfo = name.astInfo.copy();

                info.one = oneName;

                if (tree.getRule(zeroName) == null && tree.getRule(oneName) == null) {
                    PullInfo tmp = pull(decl.rhs, sym);

                    RuleDecl oneDecl = oneName.makeRule();
                    oneDecl.rhs = tmp.one.normal();
                    oneDecl.retType = name;
                    //oneDecl.isSplit = true;
                    tree.addRule(oneDecl);

                    if (tmp.zero != null) {
                        RuleDecl zeroDecl = zeroName.makeRule();
                        zeroDecl.rhs = tmp.zero.normal();
                        zeroDecl.retType = name;
                        //zeroDecl.isSplit = true;
                        tree.addRule(zeroDecl);
                        info.zero = zeroName;
                    }
                    //replace old
                    /*if (tmp.zero != null) {
                        decl.rhs = new Or(Sequence.of(sym, oneName), zeroName);
                    }
                    else {
                        decl.rhs = Sequence.of(sym, oneName);
                    }*/
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
                //A B = A_noe B | B
                Node no = new Epsilons(tree).trim(A);
                Sequence se = new Sequence(no, B);
                info = pull(new Or(se, B), sym);
            }
            else {
                //A no empty
                //(a A1 | A0) B
                //a A1 B | A0 B
                if (p1.zero != null) {
                    info.zero = makeSeq(p1.zero, B);
                }
                info.one = makeSeq(p1.one, B);
            }
        }
        else {
            //A empty,B starts
            if (A.isName() && A.asName().astInfo.isFactored) {
                PullInfo tmp = pull(B, sym);
                if (tmp.zero != null) {
                    info.zero = Sequence.of(A, tmp.zero);
                }
                info.one = Sequence.of(A, tmp.one);
            }
            else {
                //A B=A_noe B | B
                Node no = new Epsilons(tree).trim(A);
                info = pull(new Or(Sequence.of(no, B), B), sym);
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
                if (pi.zero != null) {
                    zero.add(pi.zero);
                }
            }
            else {
                zero.add(ch);
            }
        }
        if (zero.size() > 0) {
            info.zero = zero.normal();
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
        public Node one;//after factor
        public Node zero;//other
    }
}
