package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.State;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;

import java.util.*;

public class RegexBuilder {
    Tree tree;
    LLDfaBuilder builder;
    NFA dfa;
    String curRule;

    public RegexBuilder(LLDfaBuilder builder) {
        this.builder = builder;
        this.tree = builder.tree;
    }

    public Node build(String rule) {
        this.curRule = rule;
        buildNfa();
        var regex = buildRegex();
        return regex;
    }

    public void buildNfa() {
        var all = builder.rules.get(curRule);
        dfa = new NFA(all.size(), tree);
        Queue<ItemSet> queue = new LinkedList<>();
        var done = new HashSet<>();
        var first = builder.firstSets.get(curRule);
        var alphabet = dfa.getAlphabet();
        dfa.init(first.stateId);
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
        dfa = Minimization.optimize(dfa);
    }

    //remove non-factor symbols to speed up
    void trim(ItemSet set) {
        for (var tr : set.transitions) {
            if (tr.symbol.astInfo.isFactor) continue;
            var sym = tr.symbol;
            if (sym.isName() && sym.asName().isToken) {
                int which = -1;
                var target = tr.target;
                for (var item : target.all) {
                    if (!item.isReduce(tree)) continue;
                    if (item.rule.getName().equals(curRule)) {
                        which = item.rule.which;
                    }
                }
                if (which == -1) {
                    throw new RuntimeException("internal error: which not found");
                }
                sym.astInfo.which = which;
            }
            if (!sym.isSequence()) continue;
            var seq = sym.asSequence();
            List<Node> list = new ArrayList<>();
            for (var ch : seq) {
                list.add(ch);
                if (ch.isName() && ch.asName().isToken) {
                    if (!ch.astInfo.isFactor) {
                        break;
                    }
                }
            }
            tr.symbol = Sequence.make(list);
        }
    }

    Node buildRegex() {
        mergeFinals();
        for (var state : dfa.it()) {
            if (canBeRemoved(state)) {
                eliminate(state);
            }
        }
        combine();
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
                in.input = dfa.getAlphabet().addRegex(Sequence.make(list));
            }
        }
    }

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
                    list.add(dfa.getAlphabet().getRegex(tr.input));
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
