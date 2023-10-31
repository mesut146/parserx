package mesut.parserx.gen.transform;

import mesut.parserx.gen.lr.TreeInfo;
import mesut.parserx.nodes.*;

import java.util.*;

public class LrUtils {
    //convert a? into ref and make a?: a | %eps
    //convert a* into ref and...
    public static void epsilon_ref(Tree tree) {
        var tr = new Transformer(tree) {
            final Map<RuleDecl, RuleDecl> rulesOpt = new HashMap<>();
            final Map<RuleDecl, RuleDecl> rulesStar = new HashMap<>();
            final Set<Name> doneOpt = new HashSet<>();
            final Set<Name> doneStar = new HashSet<>();

            @Override
            public Node visitRegex(Regex regex, Void arg) {
                Name sym = regex.node.asName();
                if (regex.isOptional()) {
                    //a?: a | %eps
                    Name ref = new Name(sym.name + "_opt");
                    if (doneOpt.contains(sym)) return ref;
                    doneOpt.add(sym);
                    var rd = new RuleDecl(ref, new Or(new Sequence(sym), new Sequence(new Epsilon())));
                    rd.transformInfo = new TreeInfo.TransformInfo();
                    rd.transformInfo.isOpt = true;
                    rd.transformInfo.orgName = sym.name + "?";
                    rulesOpt.put(rd, curRule);
                    return ref;
                } else if (regex.isStar()) {
                    //a*: a+ | %eps
                    //a*: a* a | %eps
                    Name ref = new Name(sym.name + "_star");
                    if (doneStar.contains(sym)) return ref;
                    doneStar.add(sym);
                    //var rd = new RuleDecl(ref, new Or(new Sequence(new Regex(sym, RegexType.PLUS)), new Sequence(new Epsilon())));
                    var rd = new RuleDecl(ref, new Or(new Sequence(ref,sym), new Sequence(new Epsilon())));
                    rd.transformInfo = new TreeInfo.TransformInfo();
                    rd.transformInfo.isStar = true;
                    rd.transformInfo.orgName = sym.name + "*";
                    rulesStar.put(rd, curRule);
                    return ref;
                }
                return regex;
            }

            @Override
            public void transformRules() {
                super.transformRules();
                for (var e : rulesOpt.entrySet()) {
                    tree.addRuleBelow(e.getKey(), e.getValue());
                }
                for (var e : rulesStar.entrySet()) {
                    tree.addRuleBelow(e.getKey(), e.getValue());
                }
            }
        };
        tr.transformRules();
    }

    //make a+ ref and create a+: a+ a | a
    public static void plus(Tree tree, boolean left) {
        var tr = new Transformer(tree) {
            final Set<Name> done = new HashSet<>();
            final Map<RuleDecl, RuleDecl> ruleMap = new HashMap<>();//new rule->below

            @Override
            public Node visitRegex(Regex regex, Void arg) {
                if (!regex.isPlus()) return regex;
                var sym = regex.node.asName();
                var ref = new Name(sym.name + "_plus");
                if (done.contains(sym)) return ref;
                done.add(sym);
                RuleDecl rd;
                if (left) {
                    //a+: a+ a | a;
                    rd = new RuleDecl(ref, new Or(new Sequence(ref, sym), new Sequence(sym)));
                } else {
                    //a+: a a+ | a;
                    rd = new RuleDecl(ref, new Or(new Sequence(sym, ref), new Sequence(sym)));
                }
                rd.transformInfo = new TreeInfo.TransformInfo();
                rd.transformInfo.isPlus = true;
                rd.transformInfo.orgName = sym.name + "+";
                ruleMap.put(rd, curRule);
                return ref;
            }

            @Override
            public void transformRules() {
                super.transformRules();
                for (var e : ruleMap.entrySet()) {
                    tree.addRuleBelow(e.getKey(), e.getValue());
                }
            }
        };
        tr.transformRules();
    }

    //removes a? and a* by duplicating alt
    //a? | b = a | %empty | b
    @Deprecated
    public static void epsilon_duplicate(Tree tree) {
        var tr = new Transformer(tree) {
            @Override
            public Node visitOr(Or or, Void arg) {
                var list = new ArrayList<Node>();
                for (var ch : or) {
                    if (!ch.isSequence()) {
                        list.add(ch);
                        continue;
                    }
                    var res = visitSequence(ch.asSequence(), null);
                    if (res.isOr()) {
                        list.addAll(res.asOr().list);
                    } else {
                        list.add(res);
                    }
                }
                return Or.make(list);
            }

            @Override
            public Node visitSequence(Sequence seq, Void arg) {
                for (int i = 0; i < seq.size(); i++) {
                    var ch = seq.get(i);
                    if (ch.isOptional()) {
                        if (seq.size() == 1) {
                            return seq;
                        }
                        var regex = ch.asRegex();
                        //a b? c = a b c | a c
                        var s1 = seq.remove(i);
                        s1.list.add(i, regex.node);
                        var s2 = seq.remove(i).unwrap();
                        return visitOr(new Or(s1, s2), null);
                    } else if (ch.isStar()) {
                        var regex = ch.asRegex();
                        //a b* c = a b+ c | a c
                        var s1 = seq.remove(i);
                        s1.list.add(i, new Regex(regex.node, RegexType.PLUS));
                        var s2 = seq.remove(i).unwrap();
                        return visitOr(new Or(s1, s2), null);
                    }
                }
                return seq;
            }
        };
        tr.transformRules();
    }
}
