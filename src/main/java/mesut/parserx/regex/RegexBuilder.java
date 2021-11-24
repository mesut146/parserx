package mesut.parserx.regex;


import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
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
    List<Integer> stateOrder = new ArrayList<>();

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
        if (!nfa.findIncoming(nfa.initial).isEmpty()) {
            int newInit = nfa.newState();
            nfa.addEpsilon(newInit, nfa.initial);
            nfa.initial = newInit;
        }
        mergeFinals();

        if (stateOrder.isEmpty()) {
            autoOrder();
        }

        for (int state : stateOrder) {
            if (!nfa.isAccepting(state) && nfa.initial != state) {
                //mergeAll(state);
                eliminate(state);
            }
        }

        Or or = new Or();
        for (Transition transition : nfa.trans[nfa.initial]) {
            or.add(alphabet.getRegex(transition.input));
        }
        return or.normal();
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
        }
        else {
            Bracket bracket = new Bracket();
            bracket.add((char) range.start);
            bracket.add((char) range.end);
            return bracket;
        }
    }

    //eliminate state by removing incoming and outgoing transitions
    void eliminate(int state) {
        if (!nfa.hasTransitions(state)) return;
        List<Transition> list = nfa.trans[state];
        mergeAll(state);
        //eliminate state
        List<Transition> incomings = nfa.findIncoming(state);

        for (Transition in : incomings) {
            for (Transition out : list) {
                if (out.target == state || in.state == state) {//looping
                    continue;
                }
                Node node = path(in, out);
                if (node.isEpsilon()) {
                    nfa.addEpsilon(in.state, out.target);
                }
                else {
                    nfa.addTransition(in.state, out.target, alphabet.addRegex(node));
                }
            }
        }
        for (Transition incoming : incomings) {
            remove(incoming);
        }
        list.clear();
    }

    //if state loops itself
    Transition getLooping(int state) {
        for (Transition transition : nfa.get(state)) {
            if (transition.target == state) {
                return transition;
            }
        }
        return null;
    }

    void remove(Transition transition) {
        nfa.trans[transition.state].remove(transition);
    }

    //actual regex builder
    Node path(Transition in, Transition out) {
        Sequence path = new Sequence();
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
            path.add(new Regex(node, "*"));
        }
        if (!out.epsilon) {
            Node o = idToNode(out);
            if (o.isOr()) {
                path.add(new Group(o));
            }
            else {
                path.add(o);
            }
        }
        if (path.size() == 0) {
            return new Epsilon();
        }
        return path.normal();
    }


    //merge same targeted outgoing transitions with OrNode
    void mergeAll(int state) {
        if (!nfa.hasTransitions(state)) return;
        List<Transition> list = nfa.trans[state];
        Map<Integer, Or> map = new HashMap<>();//target -> regex
        for (Transition tr : list) {
            Or arr = map.get(tr.target);
            if (arr == null) {
                arr = new Or();
                map.put(tr.target, arr);
            }
            arr.add(idToNode(tr));
        }
        list.clear();
        for (int target : map.keySet()) {
            Node node = map.get(target).normal();
            if (node.isOr()) {
                for (Node ch : node.asOr()) {
                    if (ch.isEpsilon()) {
                        Or res = new Or(node.asOr().list);
                        res.list.remove(ch);
                        node = new Regex(res.normal(), "?");
                        break;
                    }
                }
            }
            if (node.isEpsilon()) {
                list.add(new Transition(state, target));
            }
            else {
                list.add(new Transition(state, target, alphabet.addRegex(node)));
            }
        }
    }

    //make sure we have only one final state
    int mergeFinals() {
        int newFinal = nfa.newState();
        for (int state : nfa.it()) {
            if (nfa.isAccepting(state)) {
                nfa.addEpsilon(state, newFinal);
                nfa.setAccepting(state, false);
            }
        }
        nfa.setAccepting(newFinal, true);
        return newFinal;
    }

    public void setOrder(int... arr) {
        for (int state : arr) {
            stateOrder.add(state);
        }
    }

    //simple states(non looping,no start,no final) gets eliminated first to get prettier regex
    public void autoOrder() {
        List<Integer> looping = new ArrayList<>();
        for (int state : nfa.it()) {
            if (nfa.isAccepting(state) || nfa.initial == state) continue;
            if (getLooping(state) != null) {
                looping.add(state);
            }
            else {
                stateOrder.add(state);
            }
        }
        stateOrder.addAll(looping);
    }
}
