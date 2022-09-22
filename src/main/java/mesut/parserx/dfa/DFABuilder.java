package mesut.parserx.dfa;

import java.util.*;

public class DFABuilder {
    public static boolean debugDFA = false;
    NFA nfa;
    NFA dfa;
    //nfa state set -> dfa state
    Map<StateSet, State> dfaStateMap = new HashMap<>();

    public DFABuilder(NFA nfa) {
        this.nfa = nfa;
    }

    public static NFA build(NFA nfa) {
        return new DFABuilder(nfa).dfa();
    }

    public NFA dfa() {
        if (debugDFA) {
            System.out.println("dfa conversion started");
        }
        dfa = new NFA((nfa.lastState + 1) * 2);
        dfa.init(nfa.initialState.id);
        dfa.tree = nfa.tree;

        dfaStateMap.put(closure(nfa.initialState), dfa.initialState);

        for (var mode : nfa.modes.entrySet()) {
            var modeState = mode.getValue();
            var openStates = new LinkedList<StateSet>();
            var processed = new HashSet<StateSet>();
            var cl = closure(modeState);
            var modeDfaState = getDfaState(cl);
            dfaStateMap.put(cl, modeDfaState);
            dfa.modes.put(mode.getKey(), modeDfaState);

            openStates.add(cl);

            while (!openStates.isEmpty()) {
                var curSet = openStates.poll();//current nfa state set
                var closure = closure(curSet);
                processed.add(curSet);

                //get corresponding dfa state
                var dfaState = getDfaState(closure);
                //input -> target state set
                var map = new HashMap<Integer, StateSet>();
                //find transitions from closure
                for (var epState : closure) {
                    for (var tr : epState.transitions) {
                        if (tr.epsilon) continue;
                        var targets = map.get(tr.input);//we can cache these
                        if (targets == null) {
                            targets = new StateSet();
                            map.put(tr.input, targets);
                        }
                        targets.addAll(closure(tr.target));
                    }
                }
                //make transition for each input
                for (int input : map.keySet()) {
                    var targets = map.get(input);
                    if (!openStates.contains(targets) && !processed.contains(targets)) {
                        openStates.add(closure(targets));
                    }
                    var target_state = getDfaState(targets);
                    if (debugDFA) {
                        System.out.printf("targets=%s dfa=%d\n", targets, target_state.id);
                    }
                    target_state.accepting = nfa.isAccepting(targets);
                    target_state.isSkip = nfa.isSkip(targets);
                    dfa.addTransition(dfaState, target_state, input);
                }
            }
        }
        return dfa;
    }

    //get or make dfa state for nfa state set
    State getDfaState(StateSet set) {
        var state = dfaStateMap.get(set);
        if (state == null) {
            state = dfa.newState();
            if (nfa.isAccepting(set)) {
                state.decl = nfa.getDecl(set);
                state.name = state.decl.name;
            }
            dfaStateMap.put(set, state);
        }
        return state;
    }

    public StateSet closure(State state) {
        return closure(state, new StateSet());
    }

    //epsilon closure for dfa conversion
    public StateSet closure(State state, StateSet set) {
        if (set.contains(state)) return set;
        set.addState(state);//itself
        var eps = nfa.getEps(state);
        if (eps == null || eps.states.size() == 0) {
            return set;
        }
        for (var st : eps.states) {
            closure(st, set);
        }
        return set;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(StateSet set) {
        var res = new StateSet();
        for (var state : set) {
            res.addAll(closure(state));
        }
        return res;
    }
}
