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
        Pair p = insert(decl.regex, nfa.initial);
        nfa.setAccepting(p.end, true);
        nfa.names[p.end] = decl.tokenName;
        nfa.isSkip[p.end] = decl.isSkip;
    }

    public Pair insert(Node node, int start) {
        Pair p = new Pair(start, start);
        if (node.isString()) {
            String str = node.asString().value;
            int st = start;
            int ns = start;
            for (char ch : str.toCharArray()) {
                ns = nfa.newState();
                nfa.addTransitionRange(st, ns, ch, ch);
                st = ns;
            }
            p.end = ns;
        }
        else if (node.isDot()) {
            p = insert(Dot.bracket, start);
        }
        else if (node.isBracket()) {
            Bracket b = node.asBracket();//already normalized
            int end = p.end = nfa.newState();
            //in order to have only one end state we add epsilons?
            for (int i = 0; i < b.size(); i++) {
                Range rn = b.ranges.get(i);
                nfa.addTransitionRange(start, end, rn.start, rn.end);
            }
        }
        else if (node.isSequence()) {
            Sequence seq = node.asSequence();
            int st = start;
            for (Node child : seq) {
                st = insert(child, st).end;
            }
            p.end = st;
        }
        else if (node.isRegex()) {
            Regex rn = node.asRegex();
            if (rn.isStar()) {
                int end = nfa.newState();
                nfa.addEpsilon(start, end);//zero
                Pair st = insert(rn.node, start);
                nfa.addEpsilon(st.end, start);//repeat
                p.end = end;
            }
            else if (rn.isPlus()) {
                int newState = nfa.newState();
                nfa.addEpsilon(start, newState);
                Pair st = insert(rn.node, newState);
                nfa.addEpsilon(st.end, newState);//repeat
                p = st;
            }
            else if (rn.isOptional()) {
                int end = nfa.newState();
                nfa.addEpsilon(start, end);//zero times
                Pair st = insert(rn.node, start);
                nfa.addEpsilon(st.end, end);
                p.end = end;
            }
        }
        else if (node.isOr()) {
            Or or = (Or) node;
            int end = nfa.newState();
            for (Node n : or) {
                int e = insert(n, start).end;
                nfa.addEpsilon(e, end);
            }
            p.end = end;
        }
        else if (node.isGroup()) {
            Group group = node.asGroup();
            Node rhs = group.node;
            p.end = insert(rhs, start).end;
        }
        else if (node.isName()) {//?
            //we have lexer ref just replace with target's regex
            Name name = node.asName();
            p.end = insert(tree.getToken(name.name).regex, start).end;
        }
        else if (node instanceof Until) {
            Until until = (Until) node;
            Node reg = until.node;
            if (!reg.isString()) {
                throw new RuntimeException("until node only supports strings");
            }
            //firstly add as normal string
            int ns;
            int st = start;
            int i = 0;
            for (char ch : reg.asString().value.toCharArray()) {
                ns = p.end = nfa.newState();
                nfa.addTransitionRange(st, ns, ch, ch);
                //add negated transitions
                for (Range r : until.brackets.get(i).getRanges()) {
                    nfa.addTransitionRange(st, start, r.start, r.end);
                }
                st = ns;
                i++;
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node.getClass() + " , " + node);
        }
        return p;
    }
}
