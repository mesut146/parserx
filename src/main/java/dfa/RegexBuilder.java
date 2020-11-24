package dfa;


import nodes.*;
import utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//build regex from dfa
public class RegexBuilder {

    NFA nfa;
    Alphabet alphabet;
    Alphabet regexAlphabet;

    public RegexBuilder(NFA nfa) {
        this.nfa = nfa;
        alphabet = nfa.getAlphabet();
        regexAlphabet = new Alphabet();
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
        //makeRegexAlphabet();

        for (int state = 0; state <= nfa.lastState; state++) {
            if (!nfa.isAccepting(state) && nfa.initial != state) {
                //mergeAll(state);
                eliminate(state);
                //System.out.println(state + " eliminated");
                //nfa.dump(null);
            }
        }
        OrNode orNode = new OrNode();
        for (Transition transition : nfa.trans[nfa.initial]) {
            orNode.add(alphabet.getRegex(transition.input));
        }
        //nfa.dump(null);
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
        List<Transition> list = nfa.trans[state];
        if (list != null) {
            mergeAll(state);
            //eliminate state
            List<Transition> incomings = findIncoming(state);

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
        List<Transition> list = nfa.trans[state];
        for (Transition transition : list) {
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


    void mergeAll(int st) {
        List<Transition> list = nfa.trans[st];
        if (list != null && !list.isEmpty()) {
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
                    list.add(Transition.epsilon(st, target));
                }
                else {
                    list.add(new Transition(st, target, alphabet.addRegex(or)));
                }
            }
        }
    }

    int mergeFinals() {
        int newFinal = nfa.newState();
        nfa.setAccepting(newFinal, true);
        for (int state = 0; state < nfa.lastState; state++) {
            if (nfa.isAccepting(state)) {
                List<Transition> list = nfa.trans[state];
                if (list != null) {
                    nfa.addEpsilon(state, newFinal);
                }
                nfa.setAccepting(state, false);
            }
        }
        return newFinal;
    }

    boolean hasIncoming() {
        for (int state = nfa.initial + 1; state < nfa.lastState; state++) {
            List<Transition> list = nfa.trans[state];
            if (list != null) {
                for (Transition transition : list) {
                    if (transition.target == nfa.initial) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    List<Transition> findIncoming(int to) {
        List<Transition> all = new ArrayList<>();

        for (int state = 0; state <= nfa.lastState; state++) {
            List<Transition> list = nfa.trans[state];
            if (list != null) {
                for (Transition transition : list) {
                    if (transition.target == to) {
                        all.add(transition);
                    }
                }
            }
        }
        return all;
    }

}
