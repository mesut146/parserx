package mesut.parserx.gen.transform;

import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//pull sym from node
public class Puller extends BaseVisitor<Puller.PullInfo, Name> {
    public HashSet<RuleDecl> declSet = new HashSet<>();//new rules produced by this class
    Tree tree;
    HashMap<String, PullInfo> cache = new HashMap<>();

    public Puller(Tree tree) {
        this.tree = tree;
    }

    @Override
    public PullInfo visitName(Name name, Name sym) {
        var info = new PullInfo();
        if (name.isToken) {
            info.one = new Factored(name.copy(), sym.name);
            return info;
        }
        Name zeroName = tree.getFactorZero(name, sym);
        Name oneName = tree.getFactorOne(name, new Parameter(tree.getType(sym), "arg"));
        info.one = oneName;

        //check if already pulled before
        if (tree.getRule(oneName) != null) {
            if (tree.getRule(zeroName) != null) {
                info.zero = zeroName;
            }
            return info;
        }

        var decl = tree.getRule(name);
        if (FactorHelper.hasZero(decl.rhs, sym, tree)) {
            info.zero = zeroName;
        }

        var key = name.name + "-" + sym;
        if (cache.containsKey(key)) {
            info = cache.get(key);
            info.one = info.one.copy();
            info.one.astInfo = name.astInfo.copy();
            if (info.zero != null) {
                info.zero = info.zero.copy();
                info.zero.astInfo = name.astInfo.copy();
            }
            return info;
        } else {
            cache.put(key, info);
        }

        Name symArg = sym.copy();

        Name oneRef = oneName.copy();
        oneRef.args2.clear();
        for (var arg : decl.ref.args2) {
            oneRef.args2.add(arg.copy());
        }
        oneRef.args2.add(new Parameter(tree.getType(symArg), "arg"));

        Name zeroRef = zeroName.copy();
        zeroRef.args2.clear();
        for (var arg : decl.ref.args2) {
            zeroRef.args2.add(arg.copy());
        }

        var tmp = decl.rhs.copy().accept(this, symArg);

        var oneDecl = new RuleDecl(oneRef.copy(), tmp.one);
        oneDecl.retType = decl.retType;
        tree.addRuleBelow(oneDecl, decl);
        declSet.add(oneDecl);

        if (tmp.zero != null) {
            var zeroDecl = new RuleDecl(zeroRef.copy(), tmp.zero);
            zeroDecl.retType = decl.retType;
            tree.addRuleBelow(zeroDecl, decl);
            declSet.add(zeroDecl);
        }
        return info;
    }

    @Override
    public PullInfo visitOr(Or or, Name arg) {
        List<Node> one = new ArrayList<>();
        List<Node> zero = new ArrayList<>();
        for (var ch : or) {
            if (FirstSet.start(ch, arg, tree)) {
                var pi = ch.accept(this, arg);
                one.add(pi.one);
                if (pi.zero != null) {
                    zero.add(pi.zero);
                }
            } else {
                zero.add(ch);
            }
        }
        var info = new PullInfo();
        if (!zero.isEmpty()) {
            info.zero = Or.make(zero);
        }
        info.one = Or.make(one);
        return info;
    }

    @Override
    public PullInfo visitSequence(Sequence seq, Name sym) {
        var A = seq.first();
        var B = Helper.trim(seq);
        var info = new PullInfo();
        if (FirstSet.start(A, sym, tree)) {
            var ai = A.accept(this, sym);
            if (FirstSet.canBeEmpty(A, tree) && FirstSet.start(B, sym, tree)) {
                //(a A(a) | A_no_a) B
                //a A(a) B | A_no_a B
                var s1 = new Sequence(ai.zero.copy(), B.copy());
                var s2 = new Sequence(ai.one.copy(), B.copy());
                return new Or(s1, s2).accept(this, sym);
            }
            //(a A1 | A0) B
            //a A(a) B | A_no_a B
            if (ai.zero != null) {
                info.zero = new Sequence(ai.zero.copy(), B.copy());
                info.zero.astInfo = seq.astInfo.copy();
            }
            info.one = new Sequence(ai.one.copy(), B.copy());
            info.one.astInfo = seq.astInfo.copy();
            return info;

        } else {
            //A empty,B starts
            //A B=A_noe B | A_eps B
            //A_noe B | a A_eps B(a) | A_eps B_no_a
            var a1 = Epsilons.trimInfo(A, tree);
            var pb = B.accept(this, sym);
            var s1 = a1.noEps == null ? null : Sequence.make(a1.noEps, B.copy());
            //Node s2 = a1.eps.isEpsilon() ? pb.one : new Sequence(a1.eps, pb.one);
            var s2 = new Sequence(a1.eps.copy(), pb.one);
            //Node s3 = pb.zero == null ? null : (a1.eps.isEpsilon() ? pb.zero : new Sequence(a1.eps, pb.zero));
            var s3 = pb.zero == null ? null : Sequence.make(a1.eps, pb.zero);

            if (s2.astInfo.which.isEmpty()) {
                s2.astInfo = seq.astInfo.copy();
            }

            if (s1 == null) {
                info.zero = s3;
            } else {
                s1.astInfo = seq.astInfo.copy();
                if (s3 == null) {
                    info.zero = s1;
                } else {
                    if (s3.astInfo.which.isEmpty()) {
                        s3.astInfo = seq.astInfo.copy();
                    }
                    info.zero = Or.make(s1, s3);
                }
            }
            info.one = s2;
        }
        return info;
    }

    @Override
    public PullInfo visitRegex(Regex regex, Name sym) {
        if (regex.isOptional()) {
            //A?: A | € -> A1(a) | A0 | €
            var tmp = regex.node.accept(this, sym);
            if (tmp.zero == null) {
                tmp.zero = new Epsilon();
            } else {
                tmp.zero = new Regex(tmp.zero, RegexType.OPTIONAL);
            }
            return tmp;
        } else if (regex.isStar()) {
            //A*: A A* | €
            Node star = new Regex(regex.node.copy(), RegexType.STAR);
            return new Sequence(regex.node.copy(), star).accept(this, sym);
        } else {
            //A+: A A*
            var node = new Sequence(regex.node.copy(), new Regex(regex.node.copy(), RegexType.STAR));
            return node.accept(this, sym);
        }
    }

    public static class PullInfo {
        public Node one;
        public Node zero;
    }
}
