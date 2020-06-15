package gen;

import dfa.DFA;
import dfa.Transition;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class LexerGenerator {
    DFA dfa;
    String path;
    PrintWriter writer;

    public LexerGenerator(DFA dfa, String path) {
        this.dfa = dfa;
        this.path = path;
    }

    void makeTables() {
        int[] stateToInputIndex = new int[dfa.numStates];
        int[] inputMap = new int[0];
        int[][] targetMap;//[state][input]=target

        for (int state = dfa.initial; state <= dfa.numStates; state++) {
            List<Transition> list = dfa.trans[state];
            if (list != null) {
                int inputIdx = -1;//todo;
                for (Transition tr : list) {
                    inputMap[inputIdx++] = tr.input;
                }
                inputMap[inputIdx++] = -1;//input terminator
            }
            //stateToInputIndex[];
        }
    }

    public void generate() throws FileNotFoundException {
        makeTables();
        /*writer = new PrintWriter(path);
        String className = "gen";
        writer.printf("public class %s{\n", className);


        writer.println("}");*/
    }
}
