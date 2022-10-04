package mesut.parserx.dfa;

import mesut.parserx.nodes.*;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//nfa from grammar
public class NFABuilder extends BaseVisitor<State, State> {
    NFA nfa;
    Tree tree;

    public NFABuilder(Tree tree) {
        this.tree = tree;
    }

    public static NFA build(Tree tree) {
        return new NFABuilder(tree).build();
    }

    void createModes() {
        //nfa.modes.put("DEFAULT", nfa.initialState);
        for (var tb : tree.tokenBlocks) {
            for (var mb : tb.modeBlocks) {
                nfa.modes.put(mb.name, nfa.newState());
            }
        }
    }

    public NFA build() {
        new AlphabetBuilder(tree).build();
        nfa = new NFA(100);
        nfa.init(0);
        nfa.tree = tree;
        createModes();
        for (var tb : tree.tokenBlocks) {
            for (var decl : tb.tokens) {
                if (decl.fragment) continue;
                addRegex(decl, nfa.initialState);
            }
            for (var mb : tb.modeBlocks) {
                for (var decl : mb.tokens) {
                    if (decl.fragment) continue;
                    addRegex(decl, nfa.modes.get(mb.name));
                }
            }
        }
        if (nfa.tree.options.dump) {
            var file = new File(nfa.tree.options.outDir, Utils.newName(nfa.tree.file.getName(), "-nfa.dump"));
            try {
                nfa.dump(new FileWriter(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nfa;
    }

    public void addRegex(TokenDecl decl, State start) {
        var end = decl.rhs.accept(this, start);
        end.accepting = true;
        end.isSkip = decl.isSkip;
        end.name = decl.name;
        end.decl = decl;
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

    @Override
    public State visitSub(Sub sub, State arg) {
        throw new RuntimeException("sub");
//        var eps = nfa.newState();
//        arg.addEpsilon(eps);
//        var last = nfa.lastState;
//        var end = sub.node.accept(this, eps);
//        for (int i = last + 1; i <= nfa.lastState; i++) {
//
//        }
//        return end;
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
        for (int i = 0; i < bracket.ranges.size(); i++) {
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
        if (!until.node.isString()) {
            throw new RuntimeException("until node only supports strings");
        }
        StringNode str = until.node.asString();
        var end = start;
        State newEnd = null;
        State second = null;
        int i = 0;
        for (char ch : str.value.toCharArray()) {
            newEnd = nfa.newState();
            //normal
            nfa.addTransition(end, newEnd, getRangeId(ch, ch));
            //add negated transitions
            if (i == 0) {
                second = newEnd;
            }
            if (i > 0) {
                var firstChar = str.value.charAt(0);
                nfa.addTransition(end, second, getRangeId(firstChar, firstChar));
            }
            for (var range : until.brackets.get(i).ranges) {
                nfa.addTransition(end, start, getRangeId(range.start, range.end));
            }
            end = newEnd;
            i++;
        }
        return newEnd;
    }
}
