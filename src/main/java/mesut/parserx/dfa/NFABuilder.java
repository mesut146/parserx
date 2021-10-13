package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

//nfa from grammar
public class NFABuilder {
    NFA nfa;
    Tree tree;

    public NFABuilder(Tree tree) {
        this.tree = tree;
    }

    public static NFA build(Tree tree) {
        return new NFABuilder(tree).build();
    }

    public NFA build() {
        new AlphabetBuilder(tree).build();
        nfa = new NFA(100);
        nfa.tree = tree;
        for (TokenDecl decl : tree.tokens) {
            if (!decl.fragment) {
                addRegex(decl);
            }
        }
        return nfa;
    }

    public void addRegex(TokenDecl decl) {
        int end = insert(decl.rhs, nfa.initial);
        nfa.setAccepting(end, true);
        nfa.addName(decl.name, end);
        nfa.isSkip[end] = decl.isSkip;
    }

    public int insert(Node node, int start) {
        if (node.isString()) {
            String str = node.asString().value;
            int end = start;
            int newEnd = end;
            for (char ch : str.toCharArray()) {
                newEnd = nfa.newState();
                nfa.addTransitionRange(end, newEnd, ch, ch);
                end = newEnd;
            }
            return newEnd;
        }
        else if (node.isDot()) {
            return insert(Dot.bracket, start);
        }
        else if (node.isBracket()) {
            Bracket b = node.asBracket();//already normalized
            int end = nfa.newState();
            //in order to have only one end state we add epsilons?
            for (int i = 0; i < b.size(); i++) {
                Range rn = b.ranges.get(i);
                nfa.addTransitionRange(start, end, rn.start, rn.end);
            }
            return end;
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int end = start;
            for (Node child : seq) {
                end = insert(child, end);
            }
            return end;
        }
        else if (node.isRegex()) {
            Regex rn = node.asRegex();
            if (rn.isStar()) {
                int end = nfa.newState();
                nfa.addEpsilon(start, end);//zero
                int st = insert(rn.node, start);
                nfa.addEpsilon(st, start);//repeat
                return end;
            }
            else if (rn.isPlus()) {
                int newState = nfa.newState();
                nfa.addEpsilon(start, newState);
                int end = insert(rn.node, newState);
                nfa.addEpsilon(end, newState);//repeat
                return end;
            }
            else {
                int end = nfa.newState();
                nfa.addEpsilon(start, end);//zero times
                int st = insert(rn.node, start);
                nfa.addEpsilon(st, end);
                return end;
            }
        }
        else if (node.isOr()) {
            Or or = (Or) node;
            int end = nfa.newState();
            for (Node n : or) {
                int e = insert(n, start);
                nfa.addEpsilon(e, end);
            }
            return end;
        }
        else if (node.isGroup()) {
            return insert(node.asGroup().node, start);
        }
        else if (node.isName()) {//?
            //we have lexer ref just replace with target's regex
            Name name = node.asName();
            return insert(tree.getToken(name.name).rhs, start);
        }
        else if (node instanceof Until) {
            Until until = (Until) node;
            Node reg = until.node;
            if (!reg.isString()) {
                throw new RuntimeException("until node only supports strings");
            }
            int end = start;
            int newEnd = -1;
            int i = 0;
            for (char ch : reg.asString().value.toCharArray()) {
                newEnd = nfa.newState();
                nfa.addTransitionRange(end, newEnd, ch, ch);
                //add negated transitions
                for (Range r : until.brackets.get(i).getRanges()) {
                    nfa.addTransitionRange(newEnd, start, r.start, r.end);
                }
                end = newEnd;
                i++;
            }
            return newEnd;
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass() + " , " + node);
        }
    }
}
