package lexer;

import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.nodes.Range;

import java.util.List;

public class Simulator {
    int pos;
    NFA dfa;
    int lastState;

    public Simulator(NFA dfa) {
        this.dfa = dfa;
    }

    public int nextToken(char[] input) {
        int curState = dfa.initial;
        StringBuilder buffer = new StringBuilder();
        while (true) {
            char ch;
            if (pos == input.length) {
                ch = '\0';
            }
            else {
                ch = input[pos++];
            }
            int next = getState(curState, ch, dfa);
            if (next == -1 || ch == '\0') {
                System.out.println("token=" + buffer);
                buffer.setLength(0);
                pos--;
                return 0;
            }
            else {
                curState = next;
                buffer.append(ch);
            }
        }
    }

    private int getState(int curState, char input, NFA dfa) {
        if (dfa.hasTransitions(curState)) {
            List<Transition> list = dfa.trans[curState];
            for (Transition transition : list) {
                Range range = dfa.getAlphabet().getRange(transition.input);
                if (input >= range.start && input <= range.end) {
                    return transition.target;
                }
            }
        }
        return -1;
    }

    public void simulate(String s) {
        char[] input = s.toCharArray();
        pos = 0;
        while (nextToken(input) != -1) {

        }
    }
}
