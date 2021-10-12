package mesut.parserx.gen.ll;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLDfaBuilder {
    public NFA dfa = new NFA(100);
    Tree tree;
    Map<Name, List<Integer>> finalMap = new HashMap<>();
    Map<Name, Integer> finalMapReal = new HashMap<>();
    Map<Name, Integer> startMap = new HashMap<>();
    Alphabet alphabet;

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        dfa.tree = tree;
    }

    public void build() {
        makeAlphabet();

        //make start & end states
        for (RuleDecl decl : tree.rules) {
            int start = dfa.newState();
            if (decl.ref.equals(tree.start)) {
                dfa.addEpsilon(dfa.initial, start);
            }
            startMap.put(decl.ref, start);
            List<Integer> finals = new ArrayList<>();
            finalMap.put(decl.ref, finals);
            if (decl.rhs.isOr()) {
                int realEnd = dfa.newState();
                finalMapReal.put(decl.ref, realEnd);
                for (int i = 0; i < decl.rhs.asOr().size(); i++) {
                    int curEnd = dfa.newState();
                    finals.add(curEnd);
                    dfa.setAccepting(curEnd, true);
                    dfa.names[curEnd] = decl.baseName() + (i + 1);
                    dfa.addEpsilon(curEnd, realEnd);
                    dfa.setAccepting(realEnd, true);
                    dfa.names[realEnd] = decl.baseName();
                }
            }
            else {
                int end = dfa.newState();
                finals.add(end);
                dfa.setAccepting(end, true);
                dfa.names[end] = decl.baseName();
                finalMapReal.put(decl.ref, end);
            }
        }
        for (RuleDecl decl : tree.rules) {
            add(decl);
        }
    }

    void makeAlphabet() {
        alphabet = new Alphabet();
        alphabet.lastId = 1;//skip eof
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            alphabet.addRegex(decl.ref());
        }
    }

    void add(RuleDecl decl) {
        int start = startMap.get(decl.ref);
        if (decl.rhs.isOr()) {
            int i = 0;
            for (Node ch : decl.rhs.asOr()) {
                int end0 = add(ch, start);
                int end1 = finalMap.get(decl.ref).get(i++);
                dfa.addEpsilon(end0, end1);
            }
        }
        else {
            int end = finalMap.get(decl.ref).get(0);
            dfa.names[end] = decl.baseName();
            dfa.setAccepting(end, true);
            int end0 = add(decl.rhs, start);
            int end1 = finalMap.get(decl.ref).get(0);
            dfa.addEpsilon(end0, end1);
        }
    }

    int getId(Name token) {
        return alphabet.getId(token);
    }

    int add(Node node, int start) {
        if (node.isGroup()) {
            return add(node.asGroup().node, start);
        }
        else if (node.isName()) {
            Name name = node.asName();
            if (name.isToken) {
                int end = dfa.newState();
                dfa.addTransition(start, end, getId(name));
                return end;
            }
            else {
                int end = finalMapReal.get(name);
                int s = startMap.get(name);
                dfa.addEpsilon(start, s);
                return end;
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
            int end = dfa.newState();
            for (Node ch : node.asOr()) {
                int chEnd = add(ch, start);
                dfa.addEpsilon(chEnd, end);
            }
            return end;
        }
        else if (node.isRegex()) {
            Regex regex = node.asRegex();
            int end = add(regex.node, start);
            if (regex.isOptional()) {
                dfa.addEpsilon(start, end);
                return end;
            }
            else if (regex.isStar()) {
                dfa.addEpsilon(start, end);//zero
                dfa.addEpsilon(end, start);//more
                return end;
            }
            else {
                int end2 = add(regex.node, end);
                dfa.addEpsilon(end2, end);
                return end;
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
    }
}
