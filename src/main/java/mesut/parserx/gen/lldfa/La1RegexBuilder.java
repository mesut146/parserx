package mesut.parserx.gen.lldfa;


import mesut.parserx.dfa.State;
import mesut.parserx.gen.AstInfo;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

import static mesut.parserx.gen.lr.LrDFAGen.dollar;

public class La1RegexBuilder {
    Tree tree;
    LLDfaBuilder builder;
    Name curRule;
    ItemSet initialState;
    Set<ItemSet> all;

    public La1RegexBuilder(LLDfaBuilder builder) {
        this.builder = builder;
        this.tree = builder.tree;
    }

    public Node build(Name rule) {
        this.curRule = rule;
        this.all = builder.rules.get(curRule);
        this.initialState = builder.firstSets.get(curRule);
        mergeSameSets();
        fillAlts();
        //buildNfa();
        return buildRegex();
    }

    void mergeSameSets() {
        while (true) {
            var any = false;
            for (var set1 : all) {
                //find similar sets
                var similars = new ArrayList<ItemSet>();
                for (var set2 : all) {
                    if (set2 == set1) continue;
                    if (isSame(set1, set2)) {
                        similars.add(set2);
                        //redirect other transitions to set1
                        for (var tr : set2.incoming) {
                            tr.target = set1;
                        }
                        any = true;
                    }
                }
                //remove sets and redirect other transitions to set1
                if (!similars.isEmpty()) {
                    System.out.println(set1.stateId + "," + similars.stream().map(s -> String.valueOf(s.stateId)).collect(Collectors.joining(",")));
                    similars.forEach(all::remove);
                    break;
                }
            }
            if (!any) break;
        }
    }

    boolean isSame(ItemSet set, ItemSet set2) {
        if (isFinal(set) || isFinal(set2)) return false;
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

    public void buildNfa() {
        for (var set : all) {
            trim(set);
        }
    }

    void fillAlts() {
        var deads = new HashSet<ItemSet>();
        for (var set : all) {
            for (var tr : set.transitions) {
                if (tr.symbol.astInfo.isFactor) {
                    if (tr.target.hasFinal()) {
                        tr.symbol.astInfo.which = findWhich(tr.target);
                    }
                }
                else {
                    if (!isFinal(tr.target)) {
                        deads.add(tr.target);
                    }
                    //find final set and bind this to it
                    var target = findTarget(tr.target);
                    tr.target = target;
                    tr.symbol.astInfo.which = findWhich(target);
                }
            }
        }
        all.removeAll(deads);
    }

    boolean isFinal(ItemSet set) {
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
                return with(A, sym.astInfo);
            }
        }
    }

    Node with(Node node, AstInfo info) {
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

    Node buildRegex() {
        mergeFinals();
        System.out.println("merged finals");
        builder.dump(System.out);
        eliminate();
        System.out.println("eliminated");
        builder.dump(System.out);
        for (var tr : initialState.transitions) {
            if (tr.target != initialState && !isFinal(tr.target)) {
                //right rec?
                throw new RuntimeException("error " + tr.target.stateId);
            }
        }

        //build regex
        List<Node> ors = new ArrayList<>();
        Node loop = remLoop(initialState);
        var target = initialState.transitions.get(0).target;
        Node loop2 = remLoop(target);
        List<Node> list = new ArrayList<>();
        for (var tr : initialState.transitions) {
            ors.add(tr.symbol);
        }
        if (loop != null) {
            list.add(loop);
        }
        if (loop != null || loop2 != null) {
            list.add(new Group(Or.make(ors)));
        }
        else {
            list.add(Or.make(ors));

        }
        if (loop2 != null) {
            list.add(loop2);
        }
        return Sequence.make(list);
    }

    void eliminate() {
        while (true) {
            var any = false;
            combine();
            for (var set : all) {
                if (canBeRemoved(set)) {
                    eliminate(set);
                    all.remove(set);
                    builder.dump(System.out);
                    any = true;
                    break;
                }
            }
            if (!any) {
                break;
            }
        }
    }

    Node remLoop(ItemSet state) {
        for (var tr : state.transitions) {
            if (tr.target == tr.from) {
                state.transitions.remove(tr);
                return Regex.wrap(tr.symbol, RegexType.STAR);
            }
        }
        return null;
    }

    void eliminate(ItemSet set) {
        System.out.println("eliminate " + set.stateId);
        combine();
        Node loop = null;
        LLTransition out = null;
        for (var tr : set.transitions) {
            if (tr.target.equals(set)) {
                loop = tr.symbol;
            }
            else {
                out = tr;
            }
        }
        set.transitions.remove(out);
        out.target.incoming.remove(out);
        for (var in : set.incoming) {
            in.target = out.target;
            var s1 = in.symbol.isEpsilon() ? null : wrapOr(in.symbol);
            var s2 = out.symbol.isEpsilon() ? null : wrapOr(out.symbol);
            List<Node> list = new ArrayList<>();
            if (s1 != null) list.add(s1);
            if (loop != null) {
                list.add(new Regex(new Group(wrapOr(loop)), RegexType.STAR));
            }
            if (s2 != null) list.add(s2);
            if (list.isEmpty()) {
                in.symbol = new Epsilon();
            }
            else {
                if (list.size() == 1 && list.get(0).isGroup()) {
                    in.symbol = list.get(0).asGroup().node;
                }
                else {
                    in.symbol = Sequence.make(list);
                }
            }
        }
    }

    //merge same target transitions
    void combine() {
        for (var set : all) {
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

    Node wrapOr(Node sym) {
        if (sym.isOr()) return new Group(sym);
        return sym;
    }

    public boolean canBeRemoved(ItemSet state) {
        if (isFinal(state) || state == initialState) return false;
        if (hasOneOut(state)) return true;
        //looping through final state
//        Set<Integer> visited = new HashSet<>();
//        Queue<State> queue = new LinkedList<>();
//        for (var tr : state.transitions) {
//            if (!tr.target.equals(state)) {
//                queue.add(tr.target);
//                visited.add(tr.target.id);
//            }
//        }
//        //discover
//        while (!queue.isEmpty()) {
//            var cur = queue.poll();
//            var r = reachFinal(cur, state);
//            for (var tr : cur.transitions) {
//                if (tr.target.equals(state) && r) return false;
//                if (!tr.target.equals(state) && visited.add(tr.target.id)) queue.add(tr.target);
//            }
//        }
        return false;
    }

    public boolean reachFinal(State from, State except) {
        //System.out.printf("reachFinal %d -> %d\n", from.stateId, except.stateId);
        Set<Integer> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.id);
        while (!queue.isEmpty()) {
            var cur = queue.poll();
            if (cur.accepting) return true;
            for (var tr : cur.transitions) {
                var trg = tr.target;
                if (trg.equals(except)) continue;
                if (visited.add(trg.id)) queue.add(trg);
            }
        }
        return false;
    }

    public boolean hasOneOut(ItemSet set) {
        int cnt = 0;
        for (var tr : set.transitions) {
            if (!tr.target.equals(set)) cnt++;
        }
        return cnt == 1;
    }

    void mergeFinals() {
        ItemSet acc = null;
        var rem = new ArrayList<ItemSet>();
        for (var set : all) {
            if (!isFinal(set)) continue;
            if (acc == null) {
                acc = set;
            }
            else {
                //merge into acc
                rem.add(set);
                for (var tr : set.incoming) {
                    tr.target = acc;
                }
                for (var tr : set.transitions) {
                    tr.from = acc;
                    acc.addTransition(tr);//todo check dup
                }
            }
        }
        rem.forEach(all::remove);
    }
}
