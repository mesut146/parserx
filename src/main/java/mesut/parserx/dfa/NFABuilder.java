package mesut.parserx.dfa;

import mesut.parserx.gen.PrepareLexer;
import mesut.parserx.nodes.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
        tree = new PrepareLexer(tree).prepare();
        CharClass.makeDistinctRanges(tree);
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
            Bracket b = node.asBracket().normalize();
            int end = nfa.newState();
            //in order to have only one end state we add epsilons?
            for (int i = 0; i < b.size(); i++) {
                Range rn = b.ranges.get(i);
                int left = rn.start;
                int right = rn.end;
                nfa.addTransitionRange(start, end, left, right);
            }
            p.end = end;
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
            Node r = ((Until) node).node;
            if (!r.isString()) {
                throw new RuntimeException("until node only supports strings");
            }
            int ns = nfa.newState();
            nfa.addEpsilon(start, ns);
            p.end = insert(r, ns).end;
            //trace all transitions and negate them
            Stack<Integer> stack = new Stack<>();
            stack.add(ns);
            Set<Transition> done = new HashSet<>();
            while (!stack.isEmpty()) {
                for (Transition tr : nfa.get(stack.pop())) {
                    if (done.contains(tr)) continue;
                    Node c = nfa.getAlphabet().getRegex(tr.input);
                    Bracket bracket = new Bracket();
                    bracket.add(c);
                    bracket.negate = true;

                    /*bracket.normalize();
                    bracket.clear();
                    bracket.addAll(bracket.rangeNodes);*/
                    int id = nfa.getAlphabet().addRegex(bracket);
                    done.add(nfa.addTransition(tr.target, ns, id));
                    stack.add(tr.target);
                }
            }
        }
        else {
            throw new RuntimeException("invalid node: " + node);
        }
        return p;
    }
}
