package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.State;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.Helper;
import mesut.parserx.nodes.*;

import java.util.*;

public class La1RegexBuilder {
    Tree tree;
    LLDfaBuilder builder;
    NFA dfa;
    String curRule;

    public La1RegexBuilder(LLDfaBuilder builder) {
        this.builder = builder;
        this.tree = builder.tree;
    }

    public Node build(String rule) {
        this.curRule = rule;
        removeNonFactorSets();
        buildNfa();
        return buildRegex();
    }

    public void buildNfa() {
        var all = builder.rules.get(curRule);
        var first = builder.firstSets.get(curRule);
        dfa = new NFA(all.size(), new Tree());
        dfa.init(first.stateId);
        var done = new HashSet<>();
        var alphabet = dfa.getAlphabet();
        var queue = new LinkedList<ItemSet>();
        queue.add(first);
        while (!queue.isEmpty()) {
            var set = queue.poll();
            var state = dfa.getState(set.stateId);
            trim(set);
            state.accepting = set.hasFinal();
            done.add(set);
            for (var tr : set.transitions) {
                var target = dfa.getState(tr.target.stateId);
                dfa.addTransition(state, target, alphabet.addRegex(tr.symbol));
                if (!done.contains(tr.target)) {
                    queue.add(tr.target);
                }
            }
        }
        //dfa = Minimization.optimize(dfa);
    }

    void removeNonFactorSets() {
        var all = builder.rules.get(curRule);
        for (var set : all) {
            for (var tr : set.transitions) {
                if (tr.symbol.astInfo.isFactor) continue;
                //if (tr.target.hasFinal()) continue;
                //find final set and bind this to it
                tr.target = findTarget(tr.target);
                tr.symbol.astInfo.which = findWhich(tr.target);
            }
        }
    }

    ItemSet findTarget(ItemSet set) {
        if (set.hasFinal()) {
            return set;
        }
        for (var tr : set.transitions) {
            if (tr.target == set) continue;
            return findTarget(tr.target);
        }
        return null;
    }

    //remove non-factor symbols to speed up
    void trim(ItemSet set) {
        for (var tr : set.transitions) {
            if (tr.symbol.astInfo.isFactor) continue;
            var sym = tr.symbol;
            tr.symbol = trim(sym);
        }
    }

    int findWhich(ItemSet target) {
        int which = -1;
        for (var item : target.all) {
            if (!item.isReduce(tree)) continue;
            if (item.rule.getName().equals(curRule)) {
                which = item.rule.which;
            }
        }
        if (which == -1) {
            throw new RuntimeException("internal error: which not found");
        }
        return which;
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
                return A;
            }
        }
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
        combine();
        for (var state : dfa.it()) {
            if (canBeRemoved(state)) {
                eliminate(state);
            }
        }
        combine();
        for (var state : dfa.it()) {
            if (canBeRemoved(state)) {
                eliminate(state);
            }
        }
        combine();
        //check
        for (var tr : dfa.initialState.transitions) {
            if (tr.target != dfa.initialState && !tr.target.accepting) {
                //right rec?
                throw new RuntimeException("error");
            }
        }
        //build regex
        List<Node> ors = new ArrayList<>();
        Node loop = remLoop(dfa.initialState);
        State target = dfa.initialState.transitions.get(0).target;
        Node loop2 = remLoop(target);
        List<Node> list = new ArrayList<>();
        for (var tr : dfa.initialState.transitions) {
            ors.add(dfa.getAlphabet().getRegex(tr.input));
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

    Node remLoop(State state) {
        for (var tr : state.transitions) {
            if (tr.target == tr.from) {
                state.transitions.remove(tr);
                return Regex.wrap(dfa.getAlphabet().getRegex(tr.input), RegexType.STAR);
            }
        }
        return null;
    }

    void eliminate(State state) {
        combine();
        Node loop = null;
        Transition out = null;
        for (var tr : state.transitions) {
            if (tr.target.equals(state)) {
                loop = dfa.getAlphabet().getRegex(tr.input);
            }
            else out = tr;
        }
        state.transitions.remove(out);
        out.target.incoming.remove(out);
        for (var in : state.incoming) {
            in.target = out.target;
            var s1 = in.epsilon ? null : wrapOr(dfa.getAlphabet().getRegex(in.input));
            var s2 = out.epsilon ? null : wrapOr(dfa.getAlphabet().getRegex(out.input));
            List<Node> list = new ArrayList<>();
            if (s1 != null) list.add(s1);
            if (loop != null) {
                list.add(new Regex(new Group(wrapOr(loop)), RegexType.STAR));
            }
            if (s2 != null) list.add(s2);
            if (list.isEmpty()) {
                in.epsilon = true;
            }
            else {
                if (list.size() == 1 && list.get(0).isGroup()) {
                    in.input = dfa.getAlphabet().addRegex(list.get(0).asGroup().node);
                }
                else {
                    in.input = dfa.getAlphabet().addRegex(Sequence.make(list));
                }
            }
        }
    }

    //merge same target transitions
    void combine() {
        for (var set : dfa.it()) {
            //target -> ors
            Map<State, List<Node>> map = new HashMap<>();
            Set<State> epsilons = new HashSet<>();
            for (var tr : set.transitions) {
                tr.target.incoming.remove(tr);
                var list = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                if (tr.epsilon) {
                    epsilons.add(tr.target);
                }
                else {
                    var regex = dfa.getAlphabet().getRegex(tr.input);
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
                    set.addEpsilon(trg);
                }
                else {
                    var sym = Or.make(list);
                    if (epsilons.contains(trg)) {
                        sym = new Regex(sym, RegexType.OPTIONAL);
                    }
                    set.add(new Transition(set, trg, dfa.getAlphabet().addRegex(sym)));
                }
            }
        }
    }

    Node wrapOr(Node sym) {
        if (sym.isOr()) return new Group(sym);
        return sym;
    }

    public boolean canBeRemoved(State state) {
        if (countOutgoings(state) != 1) return false;

        if (state.accepting) return false;
        //looping through final state
        Set<Integer> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        for (var tr : state.transitions) {
            if (!tr.target.equals(state)) {
                queue.add(tr.target);
                visited.add(tr.target.id);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            var cur = queue.poll();
            var r = reachFinal(cur, state);
            for (var tr : cur.transitions) {
                if (tr.target.equals(state) && r) return false;
                if (!tr.target.equals(state) && visited.add(tr.target.id)) queue.add(tr.target);
            }
        }
        return true;
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

    public static int countOutgoings(State set) {
        int cnt = 0;
        for (var tr : set.transitions) {
            if (!tr.target.equals(set)) cnt++;
        }
        return cnt;
    }

    State mergeFinals() {
        var newFinal = dfa.newState();
        for (var state : dfa.it()) {
            if (state.accepting) {
                state.addEpsilon(newFinal);
                state.accepting = false;
            }
        }
        newFinal.accepting = true;
        return newFinal;
    }
}
