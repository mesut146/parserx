package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GreedyNormalizer extends Transformer {
    Tree tree;
    boolean modified;
    FactorLoop factor;

    public GreedyNormalizer(Tree tree) {
        super(tree);
        this.tree = tree;
        factor = new FactorLoop(tree, null);
    }

    public static Set<Name> commons(Set<Name> s1, Set<Name> s2) {
        Set<Name> copy = new HashSet<>(s1);
        copy.retainAll(s2);
        return copy;
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            RuleDecl decl = tree.rules.get(i);
            modified = false;
            curRule = decl;
            factor.curRule = decl;
            decl.rhs = decl.rhs.accept(this, null);
            if (modified) {
                factor.factorize();
                i = -1;
            }
        }
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            if (i == seq.size() - 1) break;

            Node a = seq.get(i);
            Node b = seq.get(i + 1);
            if (a.astInfo.isFactored) continue;
            TailInfo sym = hasGreedyTail(a, FirstSet.firstSet(b, tree));
            if (sym == null) continue;
            modified = true;

            sym.list.add(sym.sym);
            Node cur = a;
            List<Factor.PullInfo> infos = new ArrayList<>();
            List<Node> factors = new ArrayList<>();
            for (Node node : sym.list) {
                //if (node.isOptional() || node.isStar()) continue;
                Factor.PullInfo info;
                Node f;
                if (node.isPlus() || node.isStar()) {
                    info = factor.pull(cur, node.asRegex());
                    f = node.copy();
                    f.astInfo.isFactor = true;
                    f.astInfo.factorName = factor.factor.factorName(f.asRegex().node.asName());
                }
                else {
                    if (node.isOptional()) {
                        node = node.asRegex().node;
                    }
                    info = factor.factor.pull(cur, node.asName());
                    f = node.copy();
                    f.astInfo.isFactor = true;
                    f.astInfo.factorName = factor.factor.factorName(f.asName());
                }
                factors.add(f);
                infos.add(info);
                cur = info.one;
            }
            List<Node> ors = new ArrayList<>();
            List<Node> fs = new ArrayList<>();
            for (int k = 0; k < factors.size(); k++) {
                Factor.PullInfo info = infos.get(k);
                Node f = factors.get(k);
                if (info.zero == null) {

                }
                else {
                    List<Node> s = new ArrayList<>(fs);
                    if (k == factors.size() - 1) {
                        s.add(f);
                        s.add(info.one);
                        s.add(b);
                        ors.add(new Sequence(s));
                        List<Node> s2 = new ArrayList<>(fs);
                        s2.add(info.zero);
                        s2.add(b);
                        ors.add(new Sequence(s2));
                        break;
                    }
                    else {
                        s.add(info.zero);
                        s.add(b);
                        ors.add(new Sequence(s));
                    }
                }
                fs.add(f);
            }
            Or or = new Or(ors);
            seq.list.remove(i + 1);
            seq.list.set(i, or);
            return seq;
            //throw new RuntimeException("todo greedy tail: " + sym.sym + " list: " + sym.list);
        }
        return seq;
    }

    public TailInfo hasGreedyTail(Node node, final Set<Name> first) {
        BaseVisitor<TailInfo, Void> visitor = new BaseVisitor<TailInfo, Void>() {
            @Override
            public TailInfo visitName(Name name, Void arg) {
                if (first.contains(name)) {
                    TailInfo info = new TailInfo();
                    info.sym = name;
                    return info;
                }
                if (name.isRule()) {
                    return tree.getRule(name).rhs.accept(this, arg);
                }
                return null;
            }

            @Override
            public TailInfo visitSequence(Sequence seq, Void arg) {
                Node a = seq.first();
                Node b = Helper.trim(seq);
                TailInfo info = b.accept(this, arg);
                if (info != null) {
                    info.list.add(0, a);
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
                if (regex.isOptional() || regex.isStar()) {
                    Name sym = Factor.common(first, FirstSet.firstSet(regex.node, tree));
                    if (sym == null) return null;
                    TailInfo info = new TailInfo();
                    info.sym = sym;
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


    static class TailInfo {
        Name sym;
        List<Node> list = new ArrayList<>();
    }
}
