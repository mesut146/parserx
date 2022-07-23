package mesut.parserx.dfa;

import mesut.parserx.nodes.*;

import java.util.HashMap;
import java.util.Map;

//nfa from grammar
public class NFABuilder extends BaseVisitor<State, State> {
    NFA nfa;
    Tree tree;
    Map<Name, State> finalMap = new HashMap<>();

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
        return nfa;
    }

    public void addRegex(TokenDecl decl) {
        State start;
        if (decl.after != null) {
            if (!finalMap.containsKey(decl.after)) {
                addRegex(tree.getToken(decl.after.name));
            }
            start = finalMap.get(decl.after);
        }
        else {
            start = nfa.initialState;
        }
        var end = decl.rhs.accept(this, start);
        end.accepting = true;
        end.isSkip = decl.isSkip;
        end.addName(decl.name);
        finalMap.put(decl.ref(), end);
    }

    @Override
    public State visitGroup(Group group, State start) {
        return group.node.accept(this, start);
    }

    @Override
    public State visitName(Name name, State start) {
        return tree.getToken(name.name).rhs.accept(this, start);
    }

    @Override
    public State visitSequence(Sequence seq, State start) {
        var end = start;
        for (Node ch : seq) {
            end = ch.accept(this, end);
        }
        return end;
    }

    @Override
    public State visitRegex(Regex regex, State start) {
        if (regex.isStar()) {
            var newStart = nfa.newState();
            var end = regex.node.accept(this, newStart);
            start.addEpsilon(newStart);//bind
            newStart.addEpsilon(end);//zero times
            end.addEpsilon(newStart);//repeat
            return end;
        }
        else if (regex.isPlus()) {
            var newStart = nfa.newState();
            var end = regex.node.accept(this, newStart);
            start.addEpsilon(newStart);//bind
            end.addEpsilon(newStart);//repeat
            return end;
        }
        else {
            var end = regex.node.accept(this, start);
            start.addEpsilon(end);//zero times
            return end;
        }
    }

    int getRangeId(int left, int right) {
        return nfa.getAlphabet().getId(Range.of(left, right));
    }

    @Override
    public State visitString(StringNode string, State start) {
        var end = start;
        for (char ch : string.value.toCharArray()) {
            var newEnd = nfa.newState();
            nfa.addTransition(end, newEnd, getRangeId(ch, ch));
            end = newEnd;
        }
        return end;
    }

    @Override
    public State visitDot(Dot dot, State start) {
        return Dot.bracket.accept(this, start);
    }

    @Override
    public State visitBracket(Bracket bracket, State start) {
        var end = nfa.newState();
        //in order to have only one end state we add epsilons?
        for (int i = 0; i < bracket.size(); i++) {
            var range = bracket.ranges.get(i);
            nfa.addTransition(start, end, getRangeId(range.start, range.end));
        }
        return end;
    }

    @Override
    public State visitOr(Or or, State start) {
        var end = nfa.newState();
        for (Node ch : or) {
            var chEnd = ch.accept(this, start);
            chEnd.addEpsilon(end);
        }
        return end;
    }

    @Override
    public State visitUntil(Until until, State start) {
        Node reg = until.node;
        if (!reg.isString()) {
            throw new RuntimeException("until node only supports strings");
        }
        var end = start;
        State newEnd = null;
        int i = 0;
        for (char ch : reg.asString().value.toCharArray()) {
            newEnd = nfa.newState();
            nfa.addTransition(end, newEnd, getRangeId(ch, ch));
            //add negated transitions
            for (var range : until.brackets.get(i).getRanges()) {
                nfa.addTransition(newEnd, start, getRangeId(range.start, range.end));
            }
            end = newEnd;
            i++;
        }
        return newEnd;
    }
}
