package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.State;
import mesut.parserx.dfa.Transition;
import mesut.parserx.gen.CodeWriter;
import mesut.parserx.gen.Options;
import mesut.parserx.nodes.*;

import java.util.*;

public class CcGen {
    CodeWriter w;
    RuleDecl rule;
    Tree tree;
    Options options;
    ItemSet curSet;
    LLDfaBuilder builder;
    GrammarEmitter emitter;
    NFA dfa;

    public CcGen(Tree tree) {
        this.tree = tree;
        builder = new LLDfaBuilder(tree);
        builder.factor();
        //lldfa is bad bc building regex is too hard
    }

    public void build(Name rule) {
        buildNfa(rule);
        var regex = buildRegex();
        dfa.dump();
    }

    public void buildNfa(Name rule) {
        var all = builder.rules.get(rule.name);
        dfa = new NFA(all.size(), tree);
        Queue<ItemSet> queue = new LinkedList<>();
        var done = new HashSet<>();
        var first = builder.firstSets.get(rule);
        var alphabet = dfa.getAlphabet();
        dfa.initialState = dfa.getState(first.stateId);
        queue.add(first);
        while (!queue.isEmpty()) {
            var set = queue.poll();
            var state = dfa.getState(set.stateId);
            cut(set);
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
    void cut(ItemSet set) {
        for (var tr : set.transitions) {
            if (tr.symbol.astInfo.isFactor) continue;
            var sym = tr.symbol;
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
                System.out.println("rem=" + state.state);
                eliminate(state);
            }
        }
        return null;
    }

    void eliminate(State state) {
        Node loop = null;
        Transition out = null;
        for (var tr : state.transitions) {
            if (tr.target.equals(state)) {
                loop = dfa.getAlphabet().getRegex(tr.input);
            }
            else out = tr;
        }
        for (var in : state.incoming) {
            in.target = out.target;
            out.target.incoming.remove(out);
            Node newSym;
            var s1 = wrapOr(dfa.getAlphabet().getRegex(in.input));
            var s2 = wrapOr(dfa.getAlphabet().getRegex(out.input));
            if (loop == null) {
                newSym = new Sequence(s1, s2);
            }
            else {
                newSym = new Sequence(s1, new Regex(new Group(wrapOr(loop)), RegexType.STAR), s2);
            }
            in.input = dfa.getAlphabet().addRegex(newSym);
        }
        combine();
    }

    void combine() {
        for (var set : dfa.it()) {
            //target -> ors
            Map<State, List<Node>> map = new HashMap<>();
            for (var tr : set.transitions) {
                var list = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                list.add(dfa.getAlphabet().getRegex(tr.input));
                tr.target.incoming.remove(tr);
            }
            set.transitions.clear();
            for (var trg : map.keySet()) {
                var list = map.get(trg);
                var sym = list.size() == 1 ? list.get(0) : new Or(list);
                set.add(new Transition(set, trg, dfa.getAlphabet().addRegex(sym)));
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
                visited.add(tr.target.state);
            }
        }
        //discover
        while (!queue.isEmpty()) {
            var cur = queue.poll();
            var r = reachFinal(cur, state);
            for (var tr : cur.transitions) {
                if (tr.target.equals(state) && r) return false;
                if (!tr.target.equals(state) && visited.add(tr.target.state)) queue.add(tr.target);
            }
        }
        return true;
    }

    public boolean reachFinal(State from, State except) {
        //System.out.printf("reachFinal %d -> %d\n", from.stateId, except.stateId);
        Set<Integer> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from.state);
        while (!queue.isEmpty()) {
            var cur = queue.poll();
            if (cur.accepting) return true;
            for (var tr : cur.transitions) {
                var trg = tr.target;
                if (trg.equals(except)) continue;
                if (visited.add(trg.state)) queue.add(trg);
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
