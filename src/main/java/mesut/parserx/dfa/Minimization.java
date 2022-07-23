package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.*;

public class Minimization {

    //merge transitions and inputs so that its easier to write graph
    public static NFA combineAlphabet(NFA dfa) {
        Alphabet resAlphabet = new Alphabet();
        NFA res = new NFA(dfa.lastState);
        Tree tree = new Tree();
        tree.alphabet = resAlphabet;
        res.tree = tree;
        res.initialState = dfa.initialState;
        for (var state : dfa.it()) {
            //target -> symbol
            Map<State, List<Node>> map = new HashMap<>();
            if (!state.transitions.isEmpty()) {
                for (Transition tr : state.transitions) {
                    List<Node> nodes = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                    if (tr.epsilon) {
                        nodes.add(new Epsilon());
                    }
                    else {
                        nodes.add(dfa.getAlphabet().getRegex(tr.input));
                    }
                }
                for (var target : map.keySet()) {
                    List<Node> nodes = map.get(target);//optimize()
                    Bracket bracket = new Bracket();
                    List<Node> or = new ArrayList<>();
                    for (Node node : nodes) {
                        if (node.isRange()) {
                            bracket.add(node);
                        }
                        else {
                            or.add(node);
                        }
                    }
                    if (bracket.size() > 0) {
                        or.add(bracket);
                    }
                    Node node = Or.make(or);
                    int id2;
                    if (resAlphabet.map.containsKey(node)) {
                        id2 = resAlphabet.getId(node);
                    }
                    else {
                        id2 = resAlphabet.addRegex(node);
                    }
                    res.addTransition(state, target, id2);
                }
            }
            if (state.accepting) {
                res.setAccepting(state.state, true);
            }
        }
        return res;
    }

    //https://en.wikipedia.org/wiki/DFA_minimization
    public static void removeUnreachable(NFA dfa) {
        StateSet reachable_states = new StateSet();
        StateSet new_states = new StateSet();
        reachable_states.addState(dfa.initialState);
        new_states.addState(dfa.initialState);

        do {
            StateSet temp = new StateSet();
            for (var q : new_states) {
                for (int c : dfa.getAlphabet().map.values()) {
                    for (Transition tr : q.transitions) {
                        if (tr.input == c) {
                            temp.addState(tr.target);
                        }
                    }
                }
            }
            new_states = sub(temp, reachable_states);
            reachable_states.addAll(new_states);
        } while (!new_states.states.isEmpty());

        for (var state : dfa.it()) {
            if (!reachable_states.contains(state)) {
                //remove all transitions from dead state
                state.transitions.clear();
                state.accepting = false;
            }
        }
    }

    //remove dead(non-final and no outgoing transitions)
    public static void removeDead(NFA dfa) {
        for (var state : dfa.it()) {
            if (state.accepting) continue;
            boolean dead = true;
            for (Transition tr : state.transitions) {
                //looping is not considered as transition
                if (tr.target.state != state.state) {
                    dead = false;
                    break;
                }
            }
            if (dead) {
                state.transitions.clear();
            }
        }
    }

    static StateSet sub(StateSet s1, StateSet s2) {
        StateSet set = new StateSet();
        for (var c : s1) {
            if (!s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    public static int numOfStates(NFA nfa) {
        StateSet set = new StateSet();
        for (var state : nfa.it()) {
            if (!nfa.isDead(state) && (state.accepting || !state.transitions.isEmpty())) {
                set.addState(state);
            }
        }
        return set.states.size();
    }

    //split states into accepting and non-accepting set
    static List<StateSet> group(NFA dfa) {
        List<StateSet> list = new ArrayList<>();
        StateSet noacc = new StateSet();
        Map<String, StateSet> names = new HashMap<>();
        for (var id : dfa.it()) {
            if (dfa.isDead(id)) continue;
            if (id.accepting) {
                for (String nm : id.names) {
                    if (names.containsKey(nm)) {
                        //group same token states
                        names.get(nm).addState(id);
                    }
                    else {
                        //each final state represents a different token so they can't be merged
                        StateSet acc = new StateSet();
                        acc.addState(id);
                        list.add(acc);
                        names.put(nm, acc);
                    }
                }
            }
            else {
                noacc.addState(id);
            }
        }
        list.add(noacc);
        return list;
    }

    public static NFA optimize(NFA dfa) {
        removeDead(dfa);
        removeUnreachable(dfa);
        List<StateSet> P = group(dfa);
        List<StateSet> done = new ArrayList<>();
        List<StateSet> all = new ArrayList<>(P);
        while (!P.isEmpty()) {
            StateSet set = P.get(0);
            List<State> list = new ArrayList<>(set.states);
            //get a pair
            boolean changed = false;
            main:
            //if any state pair is distinguishable then split
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    var q1 = list.get(i);
                    var q2 = list.get(j);
                    if (dist(q1, q2, all, dfa)) {
                        //split
                        StateSet s = new StateSet();
                        s.addState(q2);
                        set.remove(q2);
                        P.add(s);
                        all.add(s);
                        //System.out.println(q1 + " " + q2);
                        changed = true;
                        break main;
                    }
                }
            }
            if (!changed) {
                P.remove(0);
                done.add(set);
            }
        }
        //make new dfa
        return merge(dfa, done);
    }

    static NFA merge(NFA dfa, List<StateSet> list) {
        NFA res = new NFA(dfa.lastState + 1);
        res.initialState.state = dfa.initialState.state;
        res.tree = dfa.tree;
        //state -> target
        Map<State, State> map = new HashMap<>();
        for (StateSet set : list) {
            if (set.states.isEmpty()) continue;
            var iterator = set.iterator();
            var first = iterator.next();
            map.put(first, first);
            while (iterator.hasNext()) {
                map.put(iterator.next(), first);
            }
        }
        for (var state : dfa.it()) {
            if (dfa.isDead(state)) continue;
            var target = map.get(state);
            for (Transition tr : state.transitions) {
                res.addTransition(res.getState(target.state), res.getState(map.get(tr.target).state), tr.input);
            }
            if (state.accepting) {
                res.setAccepting(state.state, true);
            }
            var names = state.names;
            if (!names.isEmpty()) {
                var names2 = res.getState(target.state).names;
                if (names2.isEmpty()) {
                    names2.addAll(names);
                }
                else {
                    for (String nm : names) {
                        if (!names2.contains(nm)) {
                            target.addName(nm);
                        }
                    }
                }
            }
        }
        return res;
    }

    //is distinguishable
    static boolean dist(State q1, State q2, List<StateSet> P, NFA nfa) {
        for (int c : nfa.getAlphabet().map.values()) {
            var t1 = nfa.getTarget(q1, c);
            var t2 = nfa.getTarget(q2, c);
            if (t1 == null || t2 == null) continue;
            for (StateSet set : P) {
                if (set.contains(t1) && !set.contains(t2) || set.contains(t2) && !set.contains(t1)) {
                    return true;
                }
            }
        }
        return false;
    }

    //todo broken
    public static NFA Hopcroft(NFA dfa) {
        List<StateSet> P = group(dfa);
        List<StateSet> W = new ArrayList<>(P);
        while (!W.isEmpty()) {
            //get some set
            StateSet A = W.remove(0);
            for (int c : dfa.getAlphabet().map.values()) {
                StateSet X = lead(dfa, c, A);
                for (ListIterator<StateSet> it = P.listIterator(); it.hasNext(); ) {
                    StateSet Y = it.next();
                    StateSet inter = inter(X, Y);
                    StateSet sub = sub(Y, X);
                    if (!inter.states.isEmpty() && !sub.states.isEmpty()) {
                        //replace Y in P by the two sets X ∩ Y and Y \ X
                        it.remove();
                        it.add(inter);
                        it.add(sub);
                        if (W.contains(Y)) {
                            //replace Y in W by the same two sets
                            W.remove(Y);
                            W.add(inter);
                            W.add(sub);
                        }
                        else {
                            if (inter.states.size() <= sub.states.size()) {
                                W.add(inter);
                            }
                            else {
                                W.add(inter);
                            }
                        }
                    }
                }
            }
        }
        return merge(dfa, P);
    }

    static StateSet inter(StateSet s1, StateSet s2) {
        StateSet set = new StateSet();
        for (var c : s1) {
            if (s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    //if c reaches to any in target set
    static StateSet lead(NFA dfa, int c, StateSet target) {
        StateSet x = new StateSet();
        for (var s : dfa.it()) {
            for (Transition tr : s.transitions) {
                if (tr.input == c) {
                    //get all incomings and outgoings as closures
                    StateSet out = out(dfa, tr.target, new StateSet());
                    for (var o : out) {
                        if (target.contains(o)) {
                            StateSet in = in(dfa, tr.state, new StateSet());
                            x.addAll(in);
                        }
                    }
                }
            }
        }
        return x;
    }

    static StateSet in(NFA dfa, State state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        for (Transition tr : dfa.findIncoming(state)) {
            if (tr.state.state == state.state) continue;
            set.addState(tr.state);
            //closure
            in(dfa, tr.state, set);
        }
        return set;
    }

    //all outgoing states from state
    static StateSet out(NFA dfa, State state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        if (!state.transitions.isEmpty()) {
            for (Transition tr : state.transitions) {
                set.addState(tr.target);
            }
        }
        for (var s : set) {
            if (s.state == state.state) continue;
            out(dfa, s, set);
            //System.out.println(s + " ," + state);
        }
        return set;
    }
}
