package mesut.parserx.gen.transform;

import mesut.parserx.gen.lr.TreeInfo;
import mesut.parserx.nodes.*;

import java.util.*;

public class LrUtils {
    //convert a? into ref and make a?: a | %eps
    //convert a* into ref and...
    public static void epsilon(Tree tree) {
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
                    if (doneOpt.contains(sym)) return regex;
                    doneOpt.add(sym);
                    Name ref = new Name(sym.name + "_opt");
                    var rd = new RuleDecl(ref, new Or(new Sequence(sym), new Sequence(new Epsilon())));
                    rd.transformInfo = new TreeInfo.TransformInfo();
                    rd.transformInfo.isOpt = true;
                    rd.transformInfo.orgName = sym.name + "?";
                    rulesOpt.put(rd, curRule);
                    return ref;
                } else if (regex.isStar()) {
                    //a*: a+ | %eps
                    if (doneStar.contains(sym)) return regex;
                    doneStar.add(sym);
                    Name ref = new Name(sym.name + "_star");
                    var rd = new RuleDecl(ref, new Or(new Sequence(new Regex(sym, RegexType.PLUS)), new Sequence(new Epsilon())));
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
                if (done.contains(sym)) return regex;
                done.add(sym);
                var ref = new Name(sym.name + "_plus");
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

    public static void star(Tree tree, boolean left) {
        var tr = new Transformer(tree) {
            final Set<Name> done = new HashSet<>();
            final Map<RuleDecl, RuleDecl> ruleMap = new HashMap<>();

            @Override
            public Node visitSequence(Sequence seq, Void arg) {
                for (int i = 0; i < seq.size(); i++) {
                    var ch = seq.get(i);
                    if (ch.isStar()) {
                        var regex = ch.asRegex();
                        var sym = regex.node.asName();
                        //a b* c = a b' c | a b+ c
                        var prime = new Name(sym.name + "'");
                        var rd = new RuleDecl(prime, new Sequence(new Epsilon()));
                        ruleMap.put(rd, curRule);
                        var s1 = new ArrayList<>(seq.list.subList(0, i));
                        var s2 = new ArrayList<>(seq.list.subList(0, i));
                        s1.add(prime);
                        s1.addAll(seq.list.subList(i + 1, seq.size()));
                        s2.add(new Regex(sym, RegexType.PLUS));
                        s2.addAll(seq.list.subList(i + 1, seq.size()));
                        return new Or(new Sequence(s1), new Sequence(s2));
                    }
                }
                return seq;
            }
        };
        tr.transformRules();
    }
}
