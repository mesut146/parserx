package mesut.parserx.gen.transform;

import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.CountingMap2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GreedyNormalizer extends Transformer {
    public static boolean debug = true;
    Tree tree;
    boolean modified;
    Puller puller;
    CountingMap2<RuleDecl, Name> factorCount = new CountingMap2<>();

    public GreedyNormalizer(Tree tree) {
        super(tree);
        this.tree = tree;
        this.puller = new Puller(tree);
    }

    public static TailInfo hasGreedyTail(Node node, final Set<Name> first, Tree tree) {
        if (node.isName() && node.asName().isToken) {
            return null;
        }
        if (node instanceof Factored) return null;
        if (node.isName() && first.contains(node.asName())) return null;
        var visitor = new GreedyVisitor(first, tree, node);
        return node.accept(visitor, null);
    }

    public void normalize() {
        for (int i = 0; i < tree.rules.size(); i++) {
            var decl = tree.rules.get(i);
            modified = false;
            curRule = decl;
            decl.rhs = decl.rhs.accept(this, null);
            if (modified) {
                i = -1;
            }
        }
    }

    public String factorName(Name sym) {
        return sym.name + "f" + factorCount.get(curRule, sym);
    }

    @Override
    public Node visitSequence(Sequence seq, Void arg) {
        for (int i = 0; i < seq.size(); i++) {
            if (i == seq.size() - 1) break;

            Node a = seq.get(i).copy();
            Node b = seq.get(i + 1).copy();
            if (a instanceof Factored) continue;
            TailInfo sym = hasGreedyTail(a, FirstSet.firstSet(b, tree), tree);
            if (sym == null) continue;
            if (debug) {
                System.out.println("greediness in " + curRule.ref);
                System.out.println(seq);
            }
            modified = true;

            sym.list.add(sym.sym);
            Node cur = a;
            List<Puller.PullInfo> infos = new ArrayList<>();
            List<Node> factors = new ArrayList<>();
            for (Node node : sym.list) {
                if (node.isOptional() || node.isStar()) {
                    continue;
                }
                Puller.PullInfo info;
                Node f = node.copy();
                f.astInfo = new AstInfo();
                f.astInfo.isFactor = true;
                if (node.isPlus()) {
                    f.astInfo.varName = factorName(f.asRegex().node.asName());
                    //info = factor2.pull(cur.copy(), f.asRegex());
                    throw new RuntimeException("greedy loop");
                } else {
                    f.astInfo.varName = factorName(f.asName());
                    info = cur.copy().accept(puller, f.asName());
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
                var info = infos.get(k);
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
                    } else {
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
            if (debug) System.out.println("res=" + res);
            return Sequence.make(res);
        }
        return seq;
    }

    public static class TailInfo {
        public Name sym;
        public List<Node> list = new ArrayList<>();
        List<Node> loopTail = new ArrayList<>();
    }

    private static class GreedyVisitor extends BaseVisitor<TailInfo, Void> {
        private final Set<Name> first;
        private final Tree tree;
        private final Node node;
        boolean foundLoop;

        public GreedyVisitor(Set<Name> first, Tree tree, Node node) {
            this.first = first;
            this.tree = tree;
            this.node = node;
            foundLoop = false;
        }

        @Override
        public TailInfo visitName(Name name, Void arg) {
            if (first.contains(name)) {
                var info = new TailInfo();
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
            if (seq.size() == 1) return seq.get(0).accept(this, arg);
            /*if (foundLoop) {
                loopTail.addAll(seq.list);
                return null;//?
            }*/
            var a = seq.first();
            var b = Helper.trim(seq);
            var info = b.accept(this, arg);
            if (info != null) {
                info.list.add(0, a.copy());
                return info;
            }
            if (FirstSet.canBeEmpty(b, tree)) {
                //Epsilons.trimInfo(b,tree);
                return a.accept(this, arg);
            } else {
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
                var sym = FactorHelper.common(first, FirstSet.firstSet(regex.node, tree), tree);
                if (sym == null) return null;
                if (regex.isStar()) {
                    foundLoop = true;
                    //throw new RuntimeException("greedy loop in " + node + " la=" + sym);
                }
                var info = new TailInfo();
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
                var res = ch.accept(this, arg);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
    }
}
