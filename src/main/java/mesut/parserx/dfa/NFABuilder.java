package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.Map;

//nfa from grammar
public class NFABuilder extends BaseVisitor<Integer, Integer> {
    NFA nfa;
    Tree tree;
    Map<Name, Integer> finalMap = new HashMap<>();

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
            if (decl.fragment) continue;
            if (!finalMap.containsKey(decl.ref())) {
                addRegex(decl);
            }
        }
        for (TokenDecl decl : tree.tokens) {
            if (decl.fragment) continue;
            if (decl.after != null) {
                //clone initial transitions to 'after'
                for (Transition tr : nfa.get(nfa.initial)) {
                    //todo
                    //nfa.addTransition();
                }
            }
        }
        return nfa;
    }

    public void addRegex(TokenDecl decl) {
        int start;
        if (decl.after != null) {
            if (!finalMap.containsKey(decl.after)) {
                addRegex(tree.getToken(decl.after.name));
            }
            start = finalMap.get(decl.after);
        }
        else {
            start = nfa.initial;
        }
        int end = decl.rhs.accept(this, start);
        nfa.setAccepting(end, true);
        nfa.addName(decl.name, end);
        nfa.isSkip[end] = decl.isSkip;
        finalMap.put(decl.ref(), end);
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
        else if (node.isName()) {
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

    @Override
    public Integer visitGroup(Group group, Integer start) {
        return group.node.accept(this, start);
    }

    @Override
    public Integer visitName(Name name, Integer start) {
        return tree.getToken(name.name).rhs.accept(this, start);
    }

    @Override
    public Integer visitSequence(Sequence seq, Integer start) {
        int end = start;
        for (Node ch : seq) {
            end = ch.accept(this, end);
        }
        return end;
    }

    @Override
    public Integer visitRegex(Regex regex, Integer start) {
        if (regex.isStar()) {
            int newStart = nfa.newState();
            int end = regex.node.accept(this, newStart);
            nfa.addEpsilon(start, newStart);//bind
            nfa.addEpsilon(newStart, end);//zero times
            nfa.addEpsilon(end, newStart);//repeat
            return end;
        }
        else if (regex.isPlus()) {
            int newStart = nfa.newState();
            int end = regex.node.accept(this, newStart);
            nfa.addEpsilon(start, newStart);//bind
            nfa.addEpsilon(end, newStart);//repeat
            return end;
        }
        else {
            int end = regex.node.accept(this, start);
            nfa.addEpsilon(start, end);//zero times
            return end;
        }
    }

    @Override
    public Integer visitString(StringNode string, Integer start) {
        int end = start;
        for (char ch : string.value.toCharArray()) {
            int newEnd = nfa.newState();
            nfa.addTransitionRange(end, newEnd, ch, ch);
            end = newEnd;
        }
        return end;
    }

    @Override
    public Integer visitDot(Dot dot, Integer start) {
        return Dot.bracket.accept(this, start);
    }

    @Override
    public Integer visitBracket(Bracket bracket, Integer start) {
        int end = nfa.newState();
        //in order to have only one end state we add epsilons?
        for (int i = 0; i < bracket.size(); i++) {
            Range rn = bracket.ranges.get(i);
            nfa.addTransitionRange(start, end, rn.start, rn.end);
        }
        return end;
    }

    @Override
    public Integer visitOr(Or or, Integer start) {
        int end = nfa.newState();
        for (Node ch : or) {
            int chEnd = ch.accept(this, start);
            nfa.addEpsilon(chEnd, end);
        }
        return end;
    }

    @Override
    public Integer visitUntil(Until until, Integer start) {
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
}
