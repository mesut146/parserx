package mesut.parserx.regex;


import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.nodes.*;
import mesut.parserx.utils.NfaReader;
import mesut.parserx.utils.UnicodeUtils;

import java.io.IOException;
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

    public static Node fromFsm(String input) throws IOException {
        return new RegexBuilder(NfaReader.read(input)).buildRegex();
    }

    public static Node from(NFA nfa) {
        return new RegexBuilder(nfa).buildRegex();
    }

    public Node buildRegex() {
        Transition.alphabet = alphabet;
        //make initial not incoming
        if (hasIncoming()) {
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

        OrNode orNode = new OrNode();
        for (Transition transition : nfa.trans[nfa.initial]) {
            orNode.add(alphabet.getRegex(transition.input));
        }
        return orNode.normal();
    }


    Node idToNode(Transition transition) {
        if (transition.epsilon) {
            return new EmptyNode();
        }
        Node node = alphabet.getRegex(transition.input);
        if (!node.isRange()) {
            return node;
        }
        RangeNode rangeNode = node.asRange();
        if (rangeNode.isSingle()) {
            return new StringNode(UnicodeUtils.printChar(rangeNode.start));
        }
        else {
            Bracket bracket = new Bracket();
            bracket.add((char) rangeNode.start);
            bracket.add((char) rangeNode.end);
            return bracket;
        }
    }

    void eliminate(int state) {
        if (nfa.hasTransitions(state)) {
            List<Transition> list = nfa.trans[state];
            mergeAll(state);
            //eliminate state
            List<Transition> incomings = nfa.findIncoming(state);

            for (Transition in : incomings) {
                for (Transition out : list) {
                    if (out.target == state || in.state == state) {//looping
                        continue;
                    }
                    Node node;
                    node = path(in, out);
                    //System.out.println(node);
                    if (node.isEmpty()) {
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
    }

    Transition getLooping(int state) {
        if (nfa.hasTransitions(state)) {
            List<Transition> list = nfa.trans[state];
            for (Transition transition : list) {
                if (transition.target == state) {
                    return transition;
                }
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
                node = new GroupNode(node);
            }
            path.add(new RegexNode(node, "*"));
        }
        if (!out.epsilon)
            path.add(idToNode(out));
        if (path.size() == 0) {
            return new EmptyNode();
        }
        return path.normal();
    }


    //merge same targeted outgoing transitions with OrNode
    void mergeAll(int state) {
        if (nfa.hasTransitions(state)) {
            List<Transition> list = nfa.trans[state];
            Map<Integer, OrNode> map = new HashMap<>();
            for (Transition tr : list) {
                OrNode arr = map.get(tr.target);
                if (arr == null) {
                    arr = new OrNode();
                    map.put(tr.target, arr);
                }
                arr.add(idToNode(tr));
            }
            list.clear();
            for (int target : map.keySet()) {
                Node or = map.get(target).normal();
                if (or.isEmpty()) {
                    list.add(new Transition(state, target));
                }
                else {
                    list.add(new Transition(state, target, alphabet.addRegex(or)));
                }
            }
        }
    }

    //make sure we have only one final state
    int mergeFinals() {
        int newFinal = nfa.newState();
        nfa.setAccepting(newFinal, true);
        for (int state = 0; state <= nfa.lastState; state++) {
            if (nfa.isAccepting(state)) {
                if (nfa.hasTransitions(state)) {
                    nfa.addEpsilon(state, newFinal);
                }
                nfa.setAccepting(state, false);
            }
        }
        return newFinal;
    }

    boolean hasIncoming() {
        for (int state = 0; state <= nfa.lastState; state++) {
            if (nfa.hasTransitions(state)) {
                List<Transition> list = nfa.trans[state];
                for (Transition transition : list) {
                    if (transition.target == nfa.initial) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setOrder(int... arr) {
        for (int state : arr) {
            stateOrder.add(state);
        }
    }

    //simple states(non looping,no start,no final) gets eliminated first to get prettier regex
    public void autoOrder() {
        List<Integer> looping = new ArrayList<>();
        for (int state = 0; state <= nfa.lastState; state++) {
            if (!nfa.isAccepting(state) && nfa.initial != state) {
                if (getLooping(state) != null) {
                    looping.add(state);
                }
                else {
                    stateOrder.add(state);
                }
            }
        }
        stateOrder.addAll(looping);
    }
}
