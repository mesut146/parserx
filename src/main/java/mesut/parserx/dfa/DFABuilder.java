package mesut.parserx.dfa;

import java.util.*;

public class DFABuilder {
    public static boolean debugDFA = false;
    NFA nfa;
    NFA dfa;

    public DFABuilder(NFA nfa) {
        this.nfa = nfa;
    }

    public static NFA build(NFA nfa) {
        return new DFABuilder(nfa).dfa();
    }

    public NFA dfa() {
        if (debugDFA)
            System.out.println("dfa conversion started");
        dfa = new NFA(nfa.trans.length * 2);
        dfa.tree = nfa.tree;

        //nfa state set -> dfa state
        Map<StateSet, Integer> dfaStateMap = new HashMap<>();
        Queue<StateSet> openStates = new LinkedList<>();
        Set<StateSet> processed = new HashSet<>();
        openStates.add(closure(nfa.initial));
        dfa.lastState = -1;

        while (!openStates.isEmpty()) {
            StateSet curSet = openStates.poll();//current nfa state set
            StateSet closure = closure(curSet);//1,2,3
            processed.add(curSet);

            //get corresponding dfa state
            int dfaState = getDfaState(dfaStateMap, closure, dfa);
            if (debugDFA) {
                System.out.printf("Closure(%s)=%s dfa=%d\n", curSet, closure, dfaState);
            }
            //input -> target state set
            Map<Integer, StateSet> map = new HashMap<>();
            //find transitions from closure
            for (int epState : closure) {
                for (Transition t : nfa.get(epState)) {
                    if (t.epsilon) continue;
                    StateSet targets = map.get(t.input);//we can cache these
                    if (targets == null) {
                        targets = new StateSet();
                        map.put(t.input, targets);
                    }
                    targets.addAll(closure(t.target));
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
                int target_state = getDfaState(dfaStateMap, targets, dfa);
                if (debugDFA)
                    System.out.printf("targets=%s dfa=%d\n", targets, target_state);
                dfa.setAccepting(target_state, nfa.isAccepting(targets));
                dfa.addTransition(dfaState, target_state, input);
                dfa.isSkip[target_state] = nfa.isSkip(targets);
                dfa.names[target_state] = nfa.getName(targets);
            }
        }
        return dfa;
    }

    //get or make dfa state for nfa state set
    int getDfaState(Map<StateSet, Integer> map, StateSet set, NFA dfa) {
        Integer state = map.get(set);
        if (state == null) {
            state = dfa.newState();
            map.put(set, state);
        }
        return state;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(int state) {
        StateSet res = new StateSet();
        res.addState(state);//itself
        StateSet eps = nfa.getEps(state);
        if (eps == null || eps.states.size() == 0) {
            return res;
        }
        for (int st : eps.states) {
            if (!res.contains(st)) {//prevent stack overflow
                res.addAll(closure(st));
            }
        }
        return res;
    }

    //epsilon closure for dfa conversion
    public StateSet closure(StateSet set) {
        StateSet res = new StateSet();
        for (int state : set) {
            res.addAll(closure(state));
        }
        return res;
    }
}