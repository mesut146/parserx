
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.Transition;
import mesut.parserx.nodes.RangeNode;
import org.junit.Test;
import mesut.parserx.utils.IOUtils;

import java.io.FileInputStream;
import java.util.List;

public class Simulator {
    int pos;

    @Test
    public void dfa() {
        try {
            NFA dfa = NFA.makeDFA(Env.getJavaLexer());

            char[] input = IOUtils.read(new FileInputStream(Env.getFile2("/java/a.java"))).toCharArray();
            pos = 0;
            for (int i = 0; i < 100; i++) {
                nextToken(dfa, input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void nextToken(NFA dfa, char[] input) {
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
                System.out.println("token=" + buffer.toString());
                buffer.setLength(0);
                pos--;
                return;
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
                RangeNode rangeNode = dfa.getAlphabet().getRange(transition.input);
                if (input >= rangeNode.start && input <= rangeNode.end) {
                    return transition.target;
                }
            }
        }
        return -1;
    }
}
