import dfa.CharClass;
import dfa.DFA;
import dfa.Transition;

import java.io.FileReader;

public class test {

    public static void testDFA(DFA dfa) throws Exception {
        //String inputPath = "/home/mesut/IdeaProjects/parserx/test/test.java";
        String inputPath = "/home/mesut/IdeaProjects/parserx/src/Main.java";
        FileReader reader = new FileReader(inputPath);
        StringBuilder yybuf = new StringBuilder();
        int curState = dfa.initial;
        int lastState = -1;
        int yychar = '\0';
        int EOF = -1;
        int yypos = 0;
        boolean backup = false;
        while (true) {
            //System.out.println("curState=" + curState);
            if (!backup) {
                yychar = reader.read();
                yypos++;
            }
            //System.out.println("input=" + CharClass.printChar(yychar) + " val=" + yychar + " encode=" + URLEncoder.encode("" + (char) yychar));
            //find next state
            int st = getState(curState, yychar, dfa);
            if (st == -1) {
                if (lastState != -1) {
                    if (!dfa.isSkip[lastState]) {
                        System.out.printf("token=%s pos=%d name=%s\n", yybuf, yypos, dfa.names[lastState]);
                    }
                    //System.out.printf("token=%s pos=%d name=%s\n", yybuf, yypos, dfa.names[lastState]);
                    yybuf.setLength(0);
                    curState = dfa.initial;
                    backup = true;
                    lastState = -1;
                    if (yychar == EOF) {
                        break;
                    }
                }
                else {
                    throw new Exception("invalid input=" + CharClass.printChar(yychar) + " val=" + yychar + " buf=" + yybuf);
                }
            }
            else {
                yybuf.append((char) yychar);
                backup = false;
                curState = st;
                if (dfa.isAccepting(curState)) {
                    lastState = curState;
                    //int la = reader.read();
                    //int la_st = getState(curState, la, dfa);
                }
            }
        }
    }


    static int getState(int curState, int input, DFA dfa) {
        if (dfa.trans[curState] != null) {
            for (Transition tr : dfa.trans[curState]) {
                int[] seg = CharClass.desegment(tr.input);
                if (seg[0] <= input && seg[1] >= input) {
                    return tr.target;
                }
            }
        }
        return -1;
    }

}
