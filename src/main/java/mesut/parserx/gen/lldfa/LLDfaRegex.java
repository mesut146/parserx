package mesut.parserx.gen.lldfa;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.*;

import java.util.*;

public class LLDfaRegex {
    public NFA nfa = new NFA(100);
    public NFA dfa = new NFA(100);
    Tree tree;
    Map<Name, List<Integer>> finalMap = new HashMap<>();
    Map<Name, Integer> finalMapReal = new HashMap<>();
    Map<Name, Integer> startMap = new HashMap<>();
    Alphabet alphabet;
    HashSet<Name> onDemandRules = new HashSet<>();

    public LLDfaRegex(Tree tree) {
        this.tree = tree;
        dfa.tree = tree;
        nfa.tree = tree;
    }


    void makeAlphabet() {
        if (alphabet != null) return;
        alphabet = tree.alphabet = new Alphabet();
        alphabet.lastId = 1;//skip eof
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            alphabet.addRegex(decl.ref());
        }
    }

    void makeFinals(RuleDecl decl) {
        getStart(decl.ref);
        int end = getFinal(decl.ref);
        nfa.addName(decl.baseName(), end);
        List<Integer> finals = finalMap.get(decl.ref);
        if (finals == null) {
            finals = new ArrayList<>();
            finalMap.put(decl.ref, finals);
        }
        if (decl.rhs.isOr()) {
            Or or = decl.rhs.asOr();
            for (int i = 0; i < or.size(); i++) {
                int endCurCh = nfa.newState();
                nfa.addEpsilon(endCurCh, end);
                finals.add(endCurCh);
                nfa.addName(decl.baseName() + (i + 1), endCurCh);
            }
        }
        else {
            finals.add(end);
        }
    }

    void collect(RuleDecl decl) {
        new Transformer(tree) {
            @Override
            public Node visitName(Name name, Void parent) {
                if (name.isToken) return name;
                if (onDemandRules.add(name)) {
                    collect(tree.getRule(name));
                }
                return name;
            }
        }.transformNode(decl.rhs, null);
    }

    public Node makeRegex(RuleDecl decl) {
        //collect all rules that are involved
        collect(decl);
        onDemandRules.add(decl.ref);
        for (Name demand : onDemandRules) {
            makeFinals(tree.getRule(demand));
        }

        makeAlphabet();
        int start = getStart(decl.ref);
        int end = getFinal(decl.ref);

        nfa.addEpsilon(nfa.initial, start);

        if (decl.rhs.isOr()) {
            Or or = decl.rhs.asOr();
            for (int i = 0; i < or.size(); i++) {
                int end0 = add(or.get(i), start);
                int end1 = finalMap.get(decl.ref).get(i);
                nfa.addEpsilon(end0, end1);
                nfa.setAccepting(end1, true);
            }
        }
        else {
            int end0 = add(decl.rhs, start);
            nfa.addEpsilon(end0, end);
        }
        for (Name nextRule : onDemandRules) {
            if (nextRule.equals(decl.ref)) continue;
            RuleDecl rule = tree.getRule(nextRule);
            int start0 = getStart(nextRule);
            int endTmp = add(rule.rhs, start0);
            nfa.addEpsilon(endTmp, getFinal(nextRule));
        }
        dfa = nfa.dfa();
        //return RegexBuilder.from(dfa);
        return null;
    }

    int getId(Name token) {
        return alphabet.getId(token);
    }

    int getStart(Name rule) {
        if (startMap.containsKey(rule)) {
            return startMap.get(rule);
        }
        int state = nfa.newState();
        startMap.put(rule, state);
        return state;
    }

    int getFinal(Name rule) {
        if (finalMapReal.containsKey(rule)) {
            return finalMapReal.get(rule);
        }
        int state = nfa.newState();
        finalMapReal.put(rule, state);
        return state;
    }

    int add(Node node, int start) {
        if (node.isGroup()) {
            return add(node.asGroup().node, start);
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                int end = nfa.newState();
                nfa.addTransition(start, end, getId(name));
                return end;
            }
            else {
                onDemandRules.add(name);
                nfa.addEpsilon(start, getStart(name));
                return finalMapReal.get(name);
            }
        }
        else if (node.isSequence()) {
            int curEnd = start;
            for (Node ch : node.asSequence()) {
                curEnd = add(ch, curEnd);
            }
            return curEnd;
        }
        else if (node.isOr()) {
            int end = nfa.newState();
            for (Node ch : node.asOr()) {
                int chEnd = add(ch, start);
                nfa.addEpsilon(chEnd, end);
            }
            return end;
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            int end = add(regex.node, start);
            if (regex.isOptional()) {
                nfa.addEpsilon(start, end);
                return end;
            }
            else if (regex.isStar()) {
                nfa.addEpsilon(start, end);//zero
                nfa.addEpsilon(end, start);//more
                return end;
            }
            else {
                int end2 = add(regex.node, end);
                nfa.addEpsilon(end2, end);
                return end;
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }
}
