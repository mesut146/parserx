import dfa.CharClass;
import dfa.DFA;
import dfa.Transition;

import java.io.FileReader;

public class test {

    public static void testDFA(DFA dfa) throws Exception {
        String inputPath = "/home/mesut/IdeaProjects/parserx/test/test.java";
        FileReader reader = new FileReader(inputPath);
        StringBuilder yybuf = new StringBuilder();
        int curState = dfa.initial;
        int lastState = -1;
        int yychar;
        int yypos = 0;
        boolean backup = false;
        while (true) {
            if (dfa.isSkip[curState]) {
                curState = dfa.initial;
                yybuf.setLength(0);
            }
            else if (dfa.isAccepting(curState)) {
                int laChar = reader.read();
                //lookahead
                int st = getState(curState, laChar, dfa);
                if (st == -1) {//return token
                    System.out.println("token=" + yybuf.toString() + " name=" + dfa.names[curState]);
                    yybuf.setLength(0);
                    yychar = laChar;
                }
                else {//may be longer token
                    lastState = curState;
                    curState = st;
                    yybuf.append((char) laChar);
                }
            }
            //System.out.println("curState=" + curState);
            yychar = reader.read();
            yypos++;
            yybuf.append((char) yychar);
            //System.out.println("input=" + CharClass.printChar(yychar) + " val=" + yychar + " encode=" + URLEncoder.encode("" + (char) yychar));
            //find next state
            int st = getState(curState, yychar, dfa);
            if (st == -1) {
                if (lastState != -1) {
                    System.out.println("token=" + yybuf + " name=" + dfa.names[lastState]);
                    yybuf.setLength(0);
                    curState = dfa.initial;
                    backup = true;
                }
                else {
                    throw new Exception("invalid input=" + CharClass.printChar(yychar) + " val=" + yychar + " buf=" + yybuf);
                }
            }
            else {
                curState = st;
            }
        }
    }


    static int getState(int curState, int input, DFA dfa) {
        for (Transition tr : dfa.trans[curState]) {
            int[] seg = CharClass.desegment(tr.input);
            if (seg[0] <= input && seg[1] >= input) {
                return tr.target;
            }
        }
        return -1;
    }

}
