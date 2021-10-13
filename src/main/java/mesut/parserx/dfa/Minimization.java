package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.*;

public class Minimization {

    //merge transitions and inputs so that its easier to write graph
    public static NFA combineAlphabet(NFA dfa) {
        Alphabet alphabet = dfa.getAlphabet();
        Alphabet alp = new Alphabet();
        NFA res = new NFA(dfa.lastState);
        Tree tree = new Tree();
        tree.alphabet = alp;
        res.tree = tree;
        res.initial = dfa.initial;
        res.names = dfa.names;
        for (int i = dfa.initial; i <= dfa.lastState; i++) {
            //target -> symbol
            Map<Integer, List<Node>> map = new HashMap<>();
            if (dfa.hasTransitions(i)) {
                for (Transition tr : dfa.trans[i]) {
                    List<Node> nodes = map.get(tr.target);
                    if (nodes == null) {
                        nodes = new ArrayList<>();
                        map.put(tr.target, nodes);
                    }
                    if (tr.epsilon) {
                        nodes.add(new Epsilon());
                    }
                    else {
                        nodes.add(alphabet.getRegex(tr.input));
                    }
                }
                for (int target : map.keySet()) {
                    List<Node> nodes = map.get(target);//optimize()
                    Bracket bracket = new Bracket();
                    Or or = new Or();
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
                    Node node = or.normal();
                    int id;
                    if (alp.map.containsKey(node)) {
                        id = alp.getId(node);
                    }
                    else {
                        id = alp.addRegex(node);
                    }
                    res.addTransition(i, target, id);
                }
            }
            if (dfa.isAccepting(i)) {
                res.setAccepting(i, true);
            }
        }
        return res;
    }


    //https://en.wikipedia.org/wiki/DFA_minimization
    public static void removeUnreachable(NFA dfa) {
        StateSet reachable_states = new StateSet();
        StateSet new_states = new StateSet();
        reachable_states.addState(dfa.initial);
        new_states.addState(dfa.initial);

        do {
            StateSet temp = new StateSet();
            for (int q : new_states) {
                for (int c : dfa.getAlphabet().map.values()) {
                    if (!dfa.hasTransitions(q)) continue;
                    for (Transition tr : dfa.trans[q]) {
                        if (tr.input == c) {
                            temp.addState(tr.target);
                        }
                    }
                }
            }
            new_states = sub(temp, reachable_states);
            reachable_states.addAll(new_states);
        } while (!new_states.states.isEmpty());

        for (int s = dfa.initial; s <= dfa.lastState; s++) {
            if (!reachable_states.contains(s)) {
                //remove all transitions from dead state
                dfa.trans[s] = null;
                dfa.setAccepting(s, false);
            }
        }
    }

    //remove dead(non final and no outgoing transitions)
    public static void removeDead(NFA dfa) {
        for (int i = dfa.initial; i <= dfa.lastState; i++) {
            if (dfa.isAccepting(i)) continue;
            if (!dfa.hasTransitions(i)) {
                if (dfa.hasTransitions(i)) {
                    boolean dead = !dfa.hasTransitions(i);
                    for (Transition tr : dfa.get(i)) {
                        if (tr.target != i) {
                            dead = false;
                            break;
                        }
                    }
                    if (dead) {
                        System.out.println("removed dead state: " + i);
                        dfa.trans[i] = null;
                        removeDead(dfa);
                        return;
                    }
                }
            }
        }
    }

    static StateSet sub(StateSet s1, StateSet s2) {
        StateSet set = new StateSet();
        for (int c : s1) {
            if (!s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    public static int numOfStates(NFA nfa) {
        StateSet set = new StateSet();
        for (int i = nfa.initial; i <= nfa.lastState; i++) {
            if (!nfa.isDead(i) && (nfa.isAccepting(i) || nfa.hasTransitions(i))) {
                set.addState(i);
            }
        }
        return set.states.size();
    }

    //split states into accepting and non-accepting set
    static List<StateSet> group(NFA dfa) {
        List<StateSet> list = new ArrayList<>();
        StateSet noacc = new StateSet();
        Map<String, StateSet> names = new HashMap<>();
        for (int s = dfa.initial; s <= dfa.lastState; s++) {
            if (dfa.isDead(s)) continue;
            if (dfa.isAccepting(s)) {
                List<String> l = dfa.names[s];
                for (String nm : l) {
                    if (names.containsKey(nm)) {
                        //group same token states
                        names.get(nm).addState(s);
                    }
                    else {
                        //each final state represents a different token so they can't be merged
                        StateSet acc = new StateSet();
                        acc.addState(s);
                        list.add(acc);
                        names.put(nm, acc);
                    }
                }
            }
            else {
                noacc.addState(s);
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
            List<Integer> list = new ArrayList<>(set.states);
            //get a pair
            boolean changed = false;
            main:
            //if any state pair is distinguishable then split
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    int q1 = list.get(i);
                    int q2 = list.get(j);
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
        NFA d = new NFA(dfa.lastState);
        d.initial = dfa.initial;
        d.tree = dfa.tree;
        //state -> target
        Map<Integer, Integer> map = new HashMap<>();
        for (StateSet set : list) {
            Iterator<Integer> iterator = set.iterator();
            int first = iterator.next();
            map.put(first, first);
            while (iterator.hasNext()) {
                map.put(iterator.next(), first);
            }
        }
        for (int i = dfa.initial; i <= dfa.lastState; i++) {
            if (dfa.isDead(i)) continue;
            for (Transition tr : dfa.get(i)) {
                d.addTransition(map.get(i), map.get(tr.target), tr.input);
            }
            if (dfa.isAccepting(i)) {
                d.setAccepting(i, true);
            }
            if (dfa.names[i] != null) {
                if (d.names[map.get(i)] == null) {
                    d.names[map.get(i)] = dfa.names[i];
                }
                else {
                    List<String> l = dfa.names[i];
                    for (String nm : l) {
                        if (!d.names[map.get(i)].contains(nm)) {
                            d.addName(nm, map.get(i));
                        }
                    }
                }
            }
        }
        return d;
    }

    //is distinguishable
    static boolean dist(int q1, int q2, List<StateSet> P, NFA nfa) {
        for (int c : nfa.getAlphabet().map.values()) {
            int t1 = nfa.getTarget(q1, c);
            int t2 = nfa.getTarget(q2, c);
            if (t1 == -1 || t2 == -1) continue;
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
        for (int c : s1) {
            if (s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    //if c reaches to any in target set
    static StateSet lead(NFA dfa, int c, StateSet target) {
        StateSet x = new StateSet();
        for (int s = dfa.initial; s <= dfa.lastState; s++) {
            if (!dfa.hasTransitions(s)) continue;
            for (Transition tr : dfa.trans[s]) {
                if (tr.input == c) {
                    //get all incomings and outgoings as closures
                    StateSet out = out(dfa, tr.target, new StateSet());
                    for (int o : out) {
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

    static StateSet in(NFA dfa, int state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        for (Transition tr : dfa.findIncoming(state)) {
            if (tr.state == state) continue;
            set.addState(tr.state);
            //closure
            in(dfa, tr.state, set);
        }
        return set;
    }

    //all outgoing states from state
    static StateSet out(NFA dfa, int state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        if (dfa.hasTransitions(state)) {
            for (Transition tr : dfa.trans[state]) {
                set.addState(tr.target);
            }
        }
        for (int s : set) {
            if (s == state) continue;
            out(dfa, s, set);
            //System.out.println(s + " ," + state);
        }
        return set;
    }
}
