package mesut.parserx.gen.lldfa;


import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

import static mesut.parserx.gen.ParserUtils.dollar;

public class La1RegexBuilder {
    Tree tree;
    LLDfaBuilder builder;
    Name curRule;
    ItemSet initialState;
    Set<ItemSet> all;
    List<RuleDecl> rules;
    Set<Name> finals;
    Map<Set<ItemSet>, ItemSet> groups;

    public La1RegexBuilder(LLDfaBuilder builder) {
        this.builder = builder;
        this.tree = builder.tree;
    }

    public void build(Name rule) {
        this.curRule = rule;
        this.all = builder.rules.get(curRule);
        this.initialState = builder.firstSets.get(curRule);
        groups = new HashMap<>();
        System.out.println("building regex for  " + curRule);
        Name.debug = true;
        fillAlts2();
        mergeSameSets();
        makeRules();
        inline();
    }

    boolean isSingleAlt(ItemSet target) {
        var acc = findFinals(target, new HashSet<>());
        return acc.size() == 1;
    }

    Name makeRef(ItemSet set) {
        if (initialState == set) {
            return new Name(curRule.name + "_decide");
        }
        return new Name(curRule.name + "_" + set.stateId);
    }

    private void makeRules() {
        this.rules = new ArrayList<>();
        this.finals = new HashSet<>();
        for (var set : all) {
            var list = new ArrayList<Node>();
            for (var tr : set.transitions) {
                if (tr.target.transitions.isEmpty() || isSingleAlt(tr.target)) {
                    list.add(tr.symbol);
                }
                else {
                    var mapped = tr.target;
                    for (var e : groups.entrySet()) {
                        if (e.getKey().contains(tr.target)) {
                            mapped = e.getValue();
                            break;
                        }
                    }
                    if (isFinal(tr.target)) {
                        list.add(new Sequence(tr.symbol, new Regex(makeRef(mapped), RegexType.OPTIONAL)));
                    }
                    else {
                        list.add(new Sequence(tr.symbol, makeRef(mapped)));
                    }
                }

            }
            if (!list.isEmpty()) {
                rules.add(new RuleDecl(makeRef(set), Or.make(list)));
            }
            if (isFinal(set)) {
                this.finals.add(makeRef(set));
            }
        }
        System.out.println(NodeList.join(rules, "\n"));
    }

    void inline() {
        while (true) {
            var any = false;
            for (var rule : rules) {
                removeRightRec(rule);
                if (rule.ref.equals(makeRef(initialState))) continue;
                if (selfReferencing(rule)) continue;//cant inline
                //remove all references
                for (var rd : rules) {
                    if (replace(rd.rhs, rule.ref, rule.rhs)) {
                        System.out.println("inline " + rule.ref + " in " + rd);
                        any = true;
                    }
                }
                rules.remove(rule);
                break;
            }
            if (!any) {
                break;
            }
        }
    }

    boolean selfReferencing(final RuleDecl decl) {
        var tr = new Transformer() {
            boolean self = false;

            @Override
            public Node visitName(Name name, Void arg) {
                if (name.equals(decl.ref)) self = true;
                return super.visitName(name, arg);
            }
        };
        decl.rhs.accept(tr, null);
        return tr.self;
    }

    boolean replace(Node node, Name ref, Node with) {
        if (with.isOr()) with = new Group(with);
        var res = false;
        if (node.isOr()) {
            for (var ch : node.asOr()) {
                res |= replace(ch, ref, with);
            }
        }
        else if (node.isSequence()) {
            var seq = node.asSequence();
            for (int i = 0; i < seq.size(); i++) {
                var ch = seq.get(i);
                if (ch.equals(ref)) {
                    //more than one?
                    var list = new ArrayList<>(seq.list.subList(0, i));
                    if (with.isSequence()) {
                        list.addAll(with.asSequence().list);
                    }
                    else {
                        list.add(with);
                    }
                    list.addAll(seq.list.subList(i + 1, seq.list.size()));
                    seq.list = list;
                    return true;
                }
                else {
                    res |= replace(ch, ref, with);
                }
            }
        }
        else if (node.isGroup()) {
            var group = node.asGroup();
            res = replace(group.node, ref, with);
        }
        else if (node.isRegex()) {
            var regex = node.asRegex();
            if (regex.node.equals(ref)) {
                if (with.isSequence()) {
                    with = new Group(with);
                }
                regex.node = with;
                return true;
            }
            else {
                res = replace(node.asRegex().node, ref, with);
            }
        }
        return res;
    }

    void removeRightRec(RuleDecl decl) {
        if (!decl.rhs.isOr()) return;
        var or = decl.rhs.asOr();
        var prefix = new ArrayList<Node>();
        var rest = new ArrayList<Node>();
        for (int i = 0; i < or.size(); i++) {
            var ch = or.get(i);
            if (!ch.isSequence()) {
                rest.add(ch);
                continue;
            }
            var seq = ch.asSequence();
            if (seq.last().equals(decl.ref)) {
                seq.list.remove(seq.size() - 1);
                prefix.add(seq.unwrap());
            }
            else {
                rest.add(ch);
            }
        }
        if (prefix.isEmpty()) return;
        var pref = prefix.size() == 1 ? prefix.get(0) : new Or(prefix);
        var re = rest.size() == 1 ? rest.get(0) : new Group(Or.make(rest));
        decl.rhs = new Sequence(new Regex(new Group(pref), RegexType.STAR), re);
        System.out.println("right rec: " + decl);
    }

    void mergeSameSets() {
        while (true) {
            var any = false;
            for (var set1 : all) {
                //find similar sets
                var similars = new HashSet<ItemSet>();
                for (var set2 : all) {
                    if (set2 == set1) continue;
                    if (isSame(set1, set2)) {
                        similars.add(set2);
                        any = true;
                    }
                }
                //remove sets
                if (!similars.isEmpty()) {
                    System.out.println("group " + set1.stateId + "," + similars.stream().map(s -> String.valueOf(s.stateId)).collect(Collectors.joining(",")));
                    similars.forEach(all::remove);
                    groups.put(similars, set1);
                    break;
                }
            }
            if (!any) break;
        }
    }

    void redirect(ItemSet set, ItemSet target) {
        for (var tr : set.incoming) {
            tr.target = target;
        }
    }

    boolean isSame(ItemSet set, ItemSet set2) {
        //if (isFinal(set) || isFinal(set2)) return false;//why not finals?
        if (set.transitions.size() != set2.transitions.size()) return false;
        var map = new HashMap<Node, ItemSet>();
        for (var tr : set.transitions) {
            map.put(tr.symbol, tr.target);
        }
        for (var tr : set2.transitions) {
            if (!map.containsKey(tr.symbol)) return false;
            if (!map.get(tr.symbol).equals(tr.target)) return false;
        }
        return true;
    }

    void fillAlts2() {
        var deads = new HashSet<ItemSet>();
        for (var set : all) {
            //System.out.println("acc for " + set.stateId + " =" + findFinals(set, new HashSet<>()));
            for (var tr : set.transitions) {
                var acc = findFinals(tr.target, new HashSet<>());
                if (tr.target.hasFinal()) {
                    tr.symbol.astInfo.which = findWhich(tr.target);
                    if (acc.size() == 1) {
                        deads.add(tr.target);
                    }
                }
                else {
                    if (acc.size() == 1) {
                        tr.symbol.astInfo.which = acc.iterator().next();
                        deads.add(tr.target);
                    }
                }
            }
        }
        if (!deads.isEmpty()) {
            var str = deads.stream().map(set -> String.valueOf(set.stateId)).collect(Collectors.toList());
            System.out.println("deads: " + str);
            all.removeAll(deads);
        }
    }

    boolean isFinal(ItemSet set) {
        if (set.isFinal) return true;
        for (var item : set.all) {
            if (isFinal(item)) return true;
        }
        return false;
    }

    boolean isFinal(Item item) {
        return item.isReduce(tree) && item.rule.ref.equals(curRule) && item.lookAhead.contains(dollar);
    }

    ItemSet findTarget(ItemSet set) {
        if (isFinal(set)) {
            return set;
        }
        for (var tr : set.transitions) {
            if (tr.target == set) continue;
            return findTarget(tr.target);
        }
        return null;
    }

    void trim(ItemSet set) {
        for (var tr : set.transitions) {
            if (tr.symbol.astInfo.isFactor) continue;
            var sym = tr.symbol;
            tr.symbol = trim(sym);
        }
    }

    //remove non-factor symbols to speed up
    int findWhich(ItemSet target) {
        var res = target.all.stream().filter(this::isFinal).findFirst();
        if (res.isPresent()) return res.get().rule.which;
        throw new RuntimeException("which not found");
    }

    HashSet<Integer> findFinals(ItemSet set, Set<ItemSet> done) {
        if (done.contains(set)) return new HashSet<>();
        done.add(set);
        var res = new HashSet<Integer>();
        if (set.hasFinal()) {
            res.add(findWhich(set));
        }
        for (var tr : set.transitions) {
            res.addAll(findFinals(tr.target, done));
        }
        return res;
    }

    //make node la1
    Node trim(Node sym) {
        if (sym.isName()) return sym;
        if (sym.isRegex()) {
            if (!sym.asRegex().node.astInfo.isFactor) {
                return sym.asRegex().node;
            }
            return sym;
        }
        if (!sym.isSequence()) {
            throw new RuntimeException("todo trim(non seq)");
        }
        var seq = sym.asSequence();
        var A = seq.first();
        var B = Helper.trim(seq);

        if (isFactor(A)) {
            if (Helper.canBeEmpty(A, tree)) {
                return new Sequence(extract(A), trim(B));
            }
            else {
                return new Sequence(A, trim(B));
            }
        }
        else {
            if (Helper.canBeEmpty(A, tree)) {
                if (A.isStar()) {
                    return new Or(A.asRegex().node, trim(B));
                }
                return seq;
            }
            else {
                return withAst(A, sym.astInfo);
            }
        }
    }

    Node withAst(Node node, AstInfo info) {
        node = node.copy();
        if (info.which != -1) {
            node.astInfo.which = info.which;
        }
        return node;
    }

    Node extract(Node node) {
        if (node.isRegex()) return node.asRegex().node;
        return node;
    }

    boolean isFactor(Node node) {
        if (node.isRegex()) return node.asRegex().node.astInfo.isFactor;
        return node.astInfo.isFactor;
    }

    private static void combine(ItemSet set) {
        //target -> ors
        var map = new HashMap<ItemSet, List<Node>>();
        var epsilons = new HashSet<ItemSet>();
        for (var tr : set.transitions) {
            tr.target.incoming.remove(tr);
            var list = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
            if (tr.symbol.isEpsilon()) {
                epsilons.add(tr.target);
            }
            else {
                var regex = tr.symbol;
                if (regex.isOr()) {
                    list.addAll(regex.asOr().list);
                }
                else {
                    list.add(regex);
                }
            }
        }
        set.transitions.clear();
        for (var trg : map.keySet()) {
            var list = map.get(trg);
            if (list.isEmpty()) {
                set.addTransition(new Epsilon(), trg);
            }
            else {
                var sym = Or.make(list);
                if (epsilons.contains(trg)) {
                    sym = Regex.wrap(sym, RegexType.OPTIONAL);
                }
                set.addTransition(new LLTransition(set, trg, sym));
            }
        }
    }

}
