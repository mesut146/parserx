package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Minimization {

    //merge transitions and inputs so that it's easier to write graph
    public static NFA combineAlphabet(NFA dfa) {
        var resAlphabet = new Alphabet();
        var res = new NFA(dfa.lastState);
        var tree = new Tree();
        tree.alphabet = resAlphabet;
        res.tree = tree;
        res.init(dfa.initialState.id);
        for (var state : dfa.it()) {
            //target -> symbol
            Map<State, List<Node>> map = new HashMap<>();
            if (!state.transitions.isEmpty()) {
                for (var tr : state.transitions) {
                    var nodes = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
                    if (tr.epsilon) {
                        nodes.add(new Epsilon());
                    } else {
                        nodes.add(dfa.getAlphabet().getRegex(tr.input));
                    }
                }
                for (var target : map.keySet()) {
                    var nodes = map.get(target);//optimize()
                    var bracket = new Bracket();
                    List<Node> or = new ArrayList<>();
                    for (var node : nodes) {
                        if (node.isRange()) {
                            bracket.add(node.asRange());
                        } else {
                            or.add(node);
                        }
                    }
                    if (bracket.list.size() > 0) {
                        or.add(bracket);
                    }
                    var node = Or.make(or);
                    int id2;
                    if (resAlphabet.map.containsKey(node)) {
                        id2 = resAlphabet.getId(node);
                    } else {
                        id2 = resAlphabet.addRegex(node);
                    }
                    res.addTransition(state, target, id2);
                }
            }
            if (state.accepting) {
                res.setAccepting(state.id, true);
            }
        }
        return res;
    }

    //https://en.wikipedia.org/wiki/DFA_minimization
    public static void removeUnreachable(NFA dfa) {
        var reachable_states = new StateSet();
        var new_states = new StateSet();
        for (var mode : dfa.modes.values()) {
            reachable_states.addState(mode);
            new_states.addState(mode);
        }

        do {
            var temp = new StateSet();
            for (var q : new_states) {
                for (int c : dfa.getAlphabet().map.values()) {
                    for (var tr : q.transitions) {
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
            var dead = true;
            for (var tr : state.transitions) {
                //looping has no contribution
                if (tr.target.id == state.id) continue;
                dead = false;
                break;
            }
            if (dead) {
                state.transitions.clear();
                //todo incomings
            }
        }
    }

    //difference set
    static StateSet sub(StateSet s1, StateSet s2) {
        var set = new StateSet();
        for (var state : s1) {
            if (!s2.contains(state)) {
                set.addState(state);
            }
        }
        return set;
    }

    //split states into accepting and non-accepting set
    static List<StateSet> group(NFA dfa) {
        var list = new ArrayList<StateSet>();
        var noacc = new StateSet();
        var whichMap = new HashMap<String, HashMap<Integer, StateSet>>();
        for (var state : dfa.it()) {
            if (dfa.isDead(state)) continue;
            if (!state.accepting) {
                noacc.addState(state);
                continue;
            }
            var map = whichMap.computeIfAbsent(state.name, k -> new HashMap<>());
            if (map.containsKey(state.which)) {
                map.get(state.which).addState(state);
            } else {
                var set = new StateSet();
                list.add(set);
                set.addState(state);
                map.put(state.which, set);
            }
            //token name -> finals
            //var nameMap = new HashMap<String, StateSet>();
            //var name = state.name;
//            if (nameMap.containsKey(name)) {
//                //group same token states
//                nameMap.get(name).addState(state);
//            }
//            else {
//                //each final state represents different token, so they can't be merged
//                var acc = new StateSet();
//                acc.addState(state);
//                nameMap.put(name, acc);
//                list.add(acc);
//            }
        }
        list.add(noacc);
        return list;
    }

    public static NFA optimize(NFA dfa) {
        removeDead(dfa);
        removeUnreachable(dfa);
        List<StateSet> groups = group(dfa);
        List<StateSet> finalGroups = new ArrayList<>();
        List<StateSet> all = new ArrayList<>(groups);
        while (!groups.isEmpty()) {
            var group = groups.get(0);
            var groupList = new ArrayList<>(group.states);
            //get a pair
            var changed = false;
            //if any state pair is distinguishable then split
            outer:
            for (int i = 0; i < groupList.size(); i++) {
                var q1 = groupList.get(i);
                for (int j = i + 1; j < groupList.size(); j++) {
                    var q2 = groupList.get(j);
                    if (dist(q1, q2, all, dfa)) {
                        //split
                        var newSet = new StateSet();
                        newSet.addState(q2);
                        group.remove(q2);
                        groups.add(newSet);
                        all.add(newSet);
                        changed = true;
                        break outer;
                    }
                }
            }
            //indistinguishable add to finalGroup
            if (!changed) {
                groups.remove(0);
                finalGroups.add(group);
            }
        }
        //make new dfa
        return merge(dfa, finalGroups);
    }

    static NFA merge(NFA dfa, List<StateSet> groups) {
        System.out.println(groups);
        for (var state : dfa.it()) {
            var found = groups.stream().anyMatch(g -> g.contains(state));
            if (!found) {
                throw new RuntimeException("not found: " + state);
            }
        }
        var res = new NFA(dfa.lastState + 1);
        res.init(dfa.initialState.id);
        res.tree = dfa.tree;
        //todo res.modes = dfa.modes;
        //state -> target
        var map = new HashMap<State, State>();
        //make target states for groups
        for (var group : groups) {
            if (group.states.isEmpty()) continue;
            var iterator = group.iterator();
            var first = iterator.next();
            var target = res.getState(first.id);
            map.put(first, target);
            while (iterator.hasNext()) {
                map.put(iterator.next(), target);
            }
        }
        //merge all groups
        for (var state : dfa.it()) {
            if (dfa.isDead(state)) continue;
            var target = map.get(state);
            for (var tr : state.transitions) {
                var s1 = res.getState(target.id);
                var s2 = map.containsKey(tr.target) ? res.getState(map.get(tr.target).id) : res.getState(tr.target.id);
                res.addTransition(s1, s2, tr.input);
            }
            if (state.accepting) {
                res.setAccepting(state.id, true);
            }
            //set names
            if (state.name == null) continue;
            var targetState = res.getState(target.id);
            var names2 = targetState.name;
            //todo names

            if (targetState.name == null) {
                targetState.name = state.name;
            } else {
                if (!names2.equals(state.name)) {
                    targetState.name = state.name;
                }
            }
        }
        return res;
    }

    //is distinguishable
    //both have to make same tr for same inputs
    static boolean dist(State q1, State q2, List<StateSet> groups, NFA nfa) {
        for (int c : nfa.getAlphabet().map.values()) {
            var t1 = nfa.getTarget(q1, c);
            var t2 = nfa.getTarget(q2, c);
            if (t1 == null || t2 == null) continue;
            //t1 t2 must be in same group
            for (var group : groups) {
                if (group.contains(t1) && !group.contains(t2) || group.contains(t2) && !group.contains(t1)) {
                    return true;
                }
            }
        }
        return false;
    }

    //todo broken
    public static NFA Hopcroft(NFA dfa) {
        var P = group(dfa);
        var W = new ArrayList<>(P);
        while (!W.isEmpty()) {
            //get some set
            var A = W.remove(0);
            for (int c : dfa.getAlphabet().map.values()) {
                var X = lead(dfa, c, A);
                for (var it = P.listIterator(); it.hasNext(); ) {
                    var Y = it.next();
                    var inter = inter(X, Y);
                    var sub = sub(Y, X);
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
                        } else {
                            if (inter.states.size() <= sub.states.size()) {
                                W.add(inter);
                            } else {
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
        var set = new StateSet();
        for (var state : s1) {
            if (s2.contains(state)) {
                set.addState(state);
            }
        }
        return set;
    }

    //if c reaches to any in target set
    static StateSet lead(NFA dfa, int c, StateSet target) {
        var res = new StateSet();
        for (var s : dfa.it()) {
            for (var tr : s.transitions) {
                if (tr.input != c) continue;
                //get all inscoming and outgoings as closures
                var out = out(dfa, tr.target, new StateSet());
                for (var o : out) {
                    if (!target.contains(o)) continue;
                    res.addAll(in(dfa, tr.from, new StateSet()));
                }
            }
        }
        return res;
    }

    static StateSet in(NFA dfa, State state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        for (var tr : dfa.findIncoming(state)) {
            if (tr.from.id == state.id) continue;
            set.addState(tr.from);
            //closure
            in(dfa, tr.from, set);
        }
        return set;
    }

    //all outgoing states from state
    static StateSet out(NFA dfa, State state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);
        if (!state.transitions.isEmpty()) {
            for (var tr : state.transitions) {
                set.addState(tr.target);
            }
        }
        for (var s : set) {
            if (s.id == state.id) continue;
            out(dfa, s, set);
        }
        return set;
    }

    //minimize lastState by removing dead states and shifting others to new places
    void shrink(NFA dfa) {
        //todo
    }
}
