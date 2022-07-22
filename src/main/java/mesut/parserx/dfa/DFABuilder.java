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
        dfa = new NFA(nfa.lastState * 2);
        dfa.tree = nfa.tree;


        Queue<StateSet> openStates = new LinkedList<>();
        Set<StateSet> processed = new HashSet<>();
        openStates.add(closure(nfa.initialState));
        dfa.lastState = -1;

        while (!openStates.isEmpty()) {
            StateSet curSet = openStates.poll();//current nfa state set
            StateSet closure = closure(curSet);//1,2,3
            processed.add(curSet);

            //get corresponding dfa state
            var dfaState = getDfaState(closure);
            if (debugDFA) {
                System.out.printf("Closure(%s)=%s dfa=%d\n", curSet, closure, dfaState.state);
            }
            //input -> target state set
            Map<Integer, StateSet> map = new HashMap<>();
            //find transitions from closure
            for (var epState : closure) {
                for (Transition tr : epState.transitions) {
                    if (tr.epsilon) continue;
                    StateSet targets = map.get(tr.input);//we can cache these
                    if (targets == null) {
                        targets = new StateSet();
                        map.put(tr.input, targets);
                    }
                    targets.addAll(closure(tr.target));
                }
            }
            if (debugDFA)
                System.out.printf("map(%s)=%s\n", curSet, map);
            //make transition for each input
            for (int input : map.keySet()) {
                StateSet targets = map.get(input);
                if (!openStates.contains(targets) && !processed.contains(targets)) {
                    openStates.add(targets);
                }
                var target_state = getDfaState(targets);
                if (debugDFA)
                    System.out.printf("targets=%s dfa=%d\n", targets, target_state.state);
                target_state.accepting = nfa.isAccepting(targets);
                dfa.addTransition(dfaState.state, target_state.state, input);
                target_state.isSkip = nfa.isSkip(targets);
                dfa.addName(nfa.getName(targets), target_state.state);
            }
        }
        return dfa;
    }

    //get or make dfa state for nfa state set
    State getDfaState(StateSet set) {
        var state = dfaStateMap.get(set);
        if (state == null) {
            state = dfa.newState();
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
        StateSet eps = nfa.getEps(state);
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
        StateSet res = new StateSet();
        for (var state : set) {
            res.addAll(closure(state));
        }
        return res;
    }
}
