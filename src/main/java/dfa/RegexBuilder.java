package dfa;


import nodes.*;
import utils.UnicodeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        //make initial not incoming
        if (hasIncoming()) {
            int newInit = nfa.newState();
            nfa.addEpsilon(newInit, nfa.initial);
            nfa.initial = newInit;
        }
        mergeFinals();
        //makeRegexAlphabet();

        for (int state = 0; state < nfa.lastState; state++) {
            if (!nfa.isAccepting(state) && nfa.initial != state) {
                eliminate(state);
            }
        }
        OrNode orNode = new OrNode();
        for (Transition transition : nfa.trans[nfa.initial]) {
            orNode.add(alphabet.getRegex(transition.input));
        }
        //nfa.dump(null);
        return orNode.normal();
    }

    private void makeRegexAlphabet() {
        for (Iterator<RangeNode> it = alphabet.getRanges(); it.hasNext(); ) {
            RangeNode rangeNode = it.next();
            int id = alphabet.getId(rangeNode);
            Node node = idToNode(id);
            regexAlphabet.addRegex(node);
        }
    }

    Node idToNode(int id) {
        Node node = alphabet.getRegex(id);
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
                    nfa.addTransition(in.state, out.target, alphabet.addRegex(node));
                }
            }
            for (Transition incoming : incomings) {
                remove(incoming);
            }
            list.clear();
        }
        else {
            //already dead
            throw new RuntimeException("state=" + state);
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
        path.add(idToNode(in.input));
        Transition loop = getLooping(in.target);
        if (loop != null) {
            path.add(new RegexNode(idToNode(loop.input), "*"));
        }
        path.add(idToNode(out.input));
        return path.normal();
    }


    /*void mergeAll(int st) {
        for (int state = 0; state <= nfa.lastState; state++) {
            List<Transition> list = nfa.trans[state];
            if (list != null && !list.isEmpty()) {
                Map<Integer, List<Node>> map = new HashMap<>();
                for (Transition tr : list) {
                    List<Node> arr = map.get(tr.target);
                    if (arr == null) {
                        arr = new ArrayList<>();
                        map.put(tr.target, arr);
                    }
                    arr.add(tr.input);
                }
            }
        }
        for(int id:){

        }
    }*/

    int mergeFinals() {
        int newFinal = 0;
        for (int state = 0; state < nfa.lastState; state++) {
            if (nfa.isAccepting(state)) {
                List<Transition> list = nfa.trans[state];
                if (list != null) {
                    if (newFinal == 0) {
                        newFinal = nfa.newState();
                        nfa.setAccepting(newFinal, true);
                    }
                    nfa.addEpsilon(state, newFinal);
                }
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

        for (int state = 0; state < nfa.lastState; state++) {
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
