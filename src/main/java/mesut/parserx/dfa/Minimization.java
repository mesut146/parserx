package mesut.parserx.dfa;

import mesut.parserx.nodes.Bracket;
import mesut.parserx.nodes.Tree;

import java.util.*;

public class Minimization {

    public static NFA combineAlphabet(NFA dfa) {
        Alphabet alphabet = dfa.getAlphabet();
        Alphabet alp = new Alphabet();
        NFA d = new NFA(dfa.lastState);
        Tree tree = new Tree();
        tree.alphabet = alp;
        d.tree = tree;
        d.initial = dfa.initial;
        for (int i = dfa.initial; i <= dfa.lastState; i++) {
            Map<Integer, Bracket> map = new HashMap<>();//target -> symbol
            if (dfa.hasTransitions(i)) {
                for (Transition tr : dfa.trans[i]) {
                    Bracket bracket = map.get(tr.target);
                    if (bracket == null) {
                        bracket = new Bracket();
                        map.put(tr.target, bracket);
                        //alp.addRegex(bracket);
                    }
                    bracket.add(alphabet.getRange(tr.input));
                }
                for (int target : map.keySet()) {
                    Bracket b = map.get(target);
                    int id;
                    if (alp.map.containsKey(b)) {
                        id = alp.getId(b);
                    }
                    else {
                        id = alp.addRegex(b);
                    }
                    d.addTransition(i, target, id);
                }
            }
            if (dfa.isAccepting(i)) {
                d.setAccepting(i, true);
            }
        }
        return d;
    }


    //https://en.wikipedia.org/wiki/DFA_minimization
    public void removeUnreachable(NFA dfa) {
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

    public NFA Hopcroft(NFA dfa) {
        List<StateSet> P = new ArrayList<>();
        List<StateSet> W = new ArrayList<>();
        //init P,W
        StateSet acc = new StateSet();
        StateSet noacc = new StateSet();
        for (int s = dfa.initial; s <= dfa.lastState; s++) {
            if (!dfa.hasTransitions(s)) {
                continue;
            }
            if (dfa.isAccepting(s)) {
                acc.addState(s);
            }
            else {
                noacc.addState(s);
            }
        }
        P.add(acc);
        P.add(noacc);
        W.add(acc);
        W.add(noacc);
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
        System.out.println(P);
        //merge states
        Map<Integer, Integer> map = new HashMap<>();
        for (StateSet set : P) {
            Iterator<Integer> iterator = set.iterator();
            int first = iterator.next();
            map.put(first, first);
            while (iterator.hasNext()) {
                map.put(iterator.next(), first);
            }
        }
        NFA d = new NFA(dfa.lastState);
        d.initial = dfa.initial;
        d.tree = dfa.tree;
        for (int i = dfa.initial; i <= dfa.lastState; i++) {
            if (dfa.hasTransitions(i)) {
                if (map.containsKey(i)) {
                    for (Transition tr : dfa.trans[i]) {
                        int ns = map.get(i);
                        int target;
                        if (map.containsKey(tr.target)) {
                            target = map.get(tr.target);
                        }
                        else {
                            if (dfa.isAccepting(tr.target)) {
                                target = tr.target;
                            }
                            else {
                                throw new RuntimeException("no " + tr);
                            }
                        }
                        d.addTransition(ns, target, tr.input);
                    }
                }
                else {
                    //dead or final
                    /*if (dfa.isAccepting(i)) {
                        d.setAccepting(i, true);
                    }*/
                }
            }
        }
        System.out.println("before=" + numOfStates(dfa) + " after=" + numOfStates(d));
        return d;
    }

    int numOfStates(NFA nfa) {
        StateSet set = new StateSet();
        for (int i = nfa.initial; i <= nfa.lastState; i++) {
            if (nfa.isAccepting(i) || nfa.hasTransitions(i)) {
                set.addState(i);
            }
            else {
                //dead
                //throw new RuntimeException("dead " + i);
            }
        }
        return set.states.size();
    }

    StateSet inter(StateSet s1, StateSet s2) {
        StateSet set = new StateSet();
        for (int c : s1) {
            if (s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    StateSet sub(StateSet s1, StateSet s2) {
        StateSet set = new StateSet();
        for (int c : s1) {
            if (!s2.contains(c)) {
                set.addState(c);
            }
        }
        return set;
    }

    //if c reaches to any in target set
    StateSet lead(NFA dfa, int c, StateSet target) {
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

    StateSet in(NFA dfa, int state, StateSet set) {
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
    StateSet out(NFA dfa, int state, StateSet set) {
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
