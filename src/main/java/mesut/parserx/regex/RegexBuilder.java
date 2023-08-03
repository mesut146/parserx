package mesut.parserx.regex;


import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.State;
import mesut.parserx.dfa.Transition;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//build regex from dfa
public class RegexBuilder {

    NFA nfa;
    Alphabet alphabet;
    Alphabet regexAlphabet;
    List<State> stateOrder = new ArrayList<>();

    public RegexBuilder(NFA nfa) {
        this.nfa = nfa;
        alphabet = nfa.getAlphabet();
        regexAlphabet = new Alphabet();
    }

    public static Node from(NFA nfa) {
        return new RegexBuilder(nfa).buildRegex();
    }

    public Node buildRegex() {
        Transition.alphabet = alphabet;
        //make initial not incoming
        if (!nfa.findIncoming(nfa.initialState).isEmpty()) {
            var newInit = nfa.newState();
            newInit.addEpsilon(nfa.initialState);
            nfa.initialState = newInit;
        }
        mergeFinals();

        if (stateOrder.isEmpty()) {
            autoOrder();
        }

        for (var state : stateOrder) {
            if (!state.accepting && nfa.initialState.id != state.id) {
                //mergeAll(state);
                eliminate(state);
            }
        }

        List<Node> or = new ArrayList<>();
        for (Transition transition : nfa.initialState.transitions) {
            or.add(alphabet.getRegex(transition.input));
        }
        return Or.make(or);
    }


    Node idToNode(Transition transition) {
        if (transition.epsilon) {
            return new Epsilon();
        }
        Node node = alphabet.getRegex(transition.input);
        if (!node.isRange()) {
            return node;
        }
        Range range = node.asRange();
        if (range.isSingle()) {
            return new StringNode(UnicodeUtils.printChar(range.start));
        } else {
            Bracket bracket = new Bracket();
            bracket.add((char) range.start);
            bracket.add((char) range.end);
            return bracket;
        }
    }

    //eliminate state by removing incoming and outgoing transitions
    void eliminate(State state) {
        if (state.transitions.isEmpty()) return;
        mergeAll(state);
        //eliminate state
        List<Transition> incoming = nfa.findIncoming(state);
        List<Transition> list = state.transitions;

        for (Transition in : incoming) {
            for (Transition out : list) {
                if (out.target.id == state.id || in.from.id == state.id) {//looping
                    continue;
                }
                Node node = path(in, out);
                if (node.isEpsilon()) {
                    in.from.addEpsilon(out.target);
                } else {
                    nfa.addTransition(in.from, out.target, alphabet.addRegex(node));
                }
            }
        }
        for (Transition in : incoming) {
            in.from.transitions.remove(in);
        }
        list.clear();
    }

    //if state loops itself
    Transition getLooping(State state) {
        for (Transition transition : state.transitions) {
            if (transition.target.id == state.id) {
                return transition;
            }
        }
        return null;
    }

    //actual regex builder
    Node path(Transition in, Transition out) {
        List<Node> path = new ArrayList<>();
        if (!in.epsilon)
            path.add(idToNode(in));
        Transition loop = getLooping(in.target);
        if (loop != null) {
            Node node = idToNode(loop);
            if (node.isSequence()) {
                node = new Group(node);
            }
            if (node.isSequence() || node.isOr()) {
                node = new Group(node);
            }
            path.add(new Regex(node, RegexType.STAR));
        }
        if (!out.epsilon) {
            Node o = idToNode(out);
            if (o.isOr()) {
                path.add(new Group(o));
            } else {
                path.add(o);
            }
        }
        if (path.size() == 0) {
            return new Epsilon();
        }
        return Sequence.make(path);
    }


    //merge same targeted outgoing transitions with OrNode
    void mergeAll(State state) {
        if (state.transitions.isEmpty()) return;
        List<Transition> trList = state.transitions;
        Map<State, List<Node>> map = new HashMap<>();//target -> regex
        for (Transition tr : trList) {
            List<Node> arr = map.computeIfAbsent(tr.target, k -> new ArrayList<>());
            arr.add(idToNode(tr));
        }
        trList.clear();
        for (var target : map.keySet()) {
            Node node = Or.make(map.get(target));
            if (node.isOr()) {
                //trim epsilon
                for (Node ch : node.asOr()) {
                    if (ch.isEpsilon()) {
                        List<Node> list = node.asOr().list;
                        list.remove(ch);
                        node = new Regex(new Group(Or.make(list)), RegexType.OPTIONAL);
                        break;
                    }
                }
            }
            if (node.isEpsilon()) {
                trList.add(new Transition(state, target));
            } else {
                trList.add(new Transition(state, target, alphabet.addRegex(node)));
            }
        }
    }

    //make sure we have only one final state
    State mergeFinals() {
        var newFinal = nfa.newState();
        for (var state : nfa.it()) {
            if (state.accepting) {
                state.addEpsilon(newFinal);
                state.accepting = false;
            }
        }
        newFinal.accepting = true;
        return newFinal;
    }

    public void setOrder(int... arr) {
        for (var state : arr) {
            stateOrder.add(nfa.getState(state));
        }
    }

    //simple states(non-looping,non-start,non-final) gets eliminated first to get prettier regex
    public void autoOrder() {
        List<State> looping = new ArrayList<>();
        for (var state : nfa.it()) {
            if (state.accepting || nfa.initialState.id == state.id) continue;
            if (getLooping(state) != null) {
                looping.add(state);
            } else {
                stateOrder.add(state);
            }
        }
        stateOrder.addAll(looping);
    }
}
