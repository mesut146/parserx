package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class GreedyNormalizer extends Transformer {
    public static boolean debug = true;
    Tree tree;
    boolean modified;
    FactorLoop factor;

    public GreedyNormalizer(Tree tree, FactorLoop factorLoop) {
        super(tree);
        this.tree = tree;
        this.factor = factorLoop;
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            RuleDecl decl = tree.rules.get(i);
            modified = false;
            curRule = decl;
            factor.curRule = decl;
            decl.rhs = decl.rhs.accept(this, null);
            if (modified) {
                factor.factorRule(decl);
                i = -1;
            }
        }
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            if (i == seq.size() - 1) break;

            Node a = seq.get(i).copy();
            Node b = seq.get(i + 1).copy();
            if (a.astInfo.isFactored) continue;
            TailInfo sym = hasGreedyTail(a, FirstSet.firstSet(b, tree), tree, factor);
            if (sym == null) continue;
            if (debug) {
                System.out.println("greediness in " + curRule.ref);
                System.out.println(seq);
            }
            modified = true;

            sym.list.add(sym.sym);
            Node cur = a;
            List<Factor.PullInfo> infos = new ArrayList<>();
            List<Node> factors = new ArrayList<>();
            for (Node node : sym.list) {
                if (node.isOptional() || node.isStar()) {
                    continue;
                }
                Factor.PullInfo info;
                Node f = node.copy();
                f.astInfo = new AstInfo();
                f.astInfo.isFactor = true;
                if (node.isPlus()) {
                    f.astInfo.varName = factor.factor.factorName(f.asRegex().node.asName());
                    info = factor.pull(cur.copy(), f.asRegex());
                }
                else {
                    f.astInfo.varName = factor.factor.factorName(f.asName());
                    info = factor.factor.pull(cur.copy(), f.asName());
                }
                factors.add(f);
                infos.add(info);
                cur = info.one;
            }
            System.out.println("sym=" + sym.sym);
            System.out.println("factors=" + factors);
            List<Node> ors = new ArrayList<>();
            List<Node> fs = new ArrayList<>();
            for (int k = 0; k < factors.size(); k++) {
                Factor.PullInfo info = infos.get(k);
                Node f = factors.get(k);
                if (info.zero != null) {
                    List<Node> s = new ArrayList<>(fs);
                    if (k == factors.size() - 1) {
                        s.add(f);
                        s.add(info.one);
                        s.add(b.copy());
                        ors.add(new Sequence(s));
                        List<Node> s2 = new ArrayList<>(fs);
                        s2.add(info.zero);
                        s2.add(b.copy());
                        ors.add(new Sequence(s2));
                        break;
                    }
                    else {
                        s.add(info.zero);
                        s.add(b.copy());
                        ors.add(new Sequence(s));
                    }
                }
                fs.add(f);
            }
            List<Node> res = new ArrayList<>(seq.list);
            res.remove(i + 1);
            res.set(i, new Group(Or.make(ors)));
            if (debug)
                System.out.println("res=" + res);
            return Sequence.make(res);
        }
        return seq;
    }

    public static TailInfo hasGreedyTail(Node node, final Set<Name> first, Tree tree, FactorLoop factor) {
        if (node.isName() && node.asName().isToken) {
            return null;
        }
        if (node.isName() && first.contains(node.asName())) return null;
        //final List<Node> loopTail = new ArrayList<>();
        BaseVisitor<TailInfo, Void> visitor = new BaseVisitor<TailInfo, Void>() {
            boolean foundLoop = false;

            @Override
            public TailInfo visitName(Name name, Void arg) {
                if (first.contains(name)) {
                    TailInfo info = new TailInfo();
                    info.sym = name.copy();
                    return info;
                }
                if (name.isRule()) {
                    return tree.getRule(name).rhs.accept(this, arg);
                }
                return null;
            }

            @Override
            public TailInfo visitSequence(Sequence seq, Void arg) {
                /*if (foundLoop) {
                    loopTail.addAll(seq.list);
                    return null;//?
                }*/
                Node a = seq.first();
                Node b = Helper.trim(seq);
                TailInfo info = b.accept(this, arg);
                if (info != null) {
                    info.list.add(0, a.copy());
                    return info;
                }
                if (FirstSet.canBeEmpty(b, tree)) {
                    //Epsilons.trimInfo(b,tree);
                    return a.accept(this, arg);
                }
                else {
                    return null;
                }
            }

            @Override
            public TailInfo visitGroup(Group group, Void arg) {
                return group.node.accept(this, arg);
            }

            @Override
            public TailInfo visitRegex(Regex regex, Void arg) {
                //todo plus
                if (regex.isOptional() || regex.isStar()) {
                    Name sym = factor.helper.common(first, FirstSet.firstSet(regex.node, tree));
                    if (sym == null) return null;
                    if (regex.isStar()) {
                        foundLoop = true;
                        throw new RuntimeException("greedy loop in " + node + " la=" + sym);
                    }
                    TailInfo info = new TailInfo();
                    info.sym = sym.copy();
                    return info;
                }
                return regex.node.accept(this, arg);
            }

            @Override
            public TailInfo visitOr(Or or, Void arg) {
                if (!FirstSet.canBeEmpty(or, tree)) {
                    return null;
                }
                for (Node ch : or) {
                    TailInfo res = ch.accept(this, arg);
                    if (res != null) {
                        return res;
                    }
                }
                return null;
            }
        };
        return node.accept(visitor, null);
    }


    public static class TailInfo {
        Name sym;
        List<Node> list = new ArrayList<>();
        List<Node> loopTail = new ArrayList<>();
    }
}
