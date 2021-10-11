package mesut.parserx.gen.ll;

import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class LLDfaBuilder {
    public NFA dfa = new NFA(100);
    Tree tree;
    Map<Name, Integer> finalMap = new HashMap<>();
    Map<Name, Integer> startMap = new HashMap<>();

    public LLDfaBuilder(Tree tree) {
        this.tree = tree;
        dfa.tree = tree;
    }

    public void build() {
        makeAlphabet();
        for (RuleDecl decl : tree.rules) {
            add(decl);
        }
    }

    void makeAlphabet() {
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            tree.alphabet.addRegex(decl.ref());
        }
    }

    int add(RuleDecl decl) {
        int start = dfa.newState();
        startMap.put(decl.ref, start);
        int end = dfa.newState();
        finalMap.put(decl.ref, end);
        dfa.names[end] = decl.baseName();
        dfa.setAccepting(end, true);

        int end2 = add(decl.rhs, start);
        dfa.addEpsilon(end2, end);
        return end;
    }

    int getId(Name token) {
        return tree.alphabet.getId(token);
    }

    int add(Node node, int start) {
        //todo merge start nodes
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
                Integer tmpEnd = finalMap.get(name);
                if (tmpEnd != null) {
                    //already inserted
                    //todo what if
                    dfa.addEpsilon(tmpEnd, start);
                    dfa.addEpsilon(start, tmpEnd);
                    return tmpEnd;
                }
                else {
                    int end = dfa.newState();
                    //dfa.addEpsilon(start, start2);
                    return end;
                }
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
