import dfa.Alphabet;
import dfa.NFA;
import nodes.StringNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NfaReader {

    public static NFA read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        NFA nfa = new NFA(100);
        String line;
        Alphabet alphabet = new Alphabet();
        nfa.tree.alphabet = alphabet;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            int arrow = line.indexOf("->");
            int comma = line.indexOf(",");
            int[] state = getState(line.substring(0, arrow));
            int[] target = getState(line.substring(arrow + 2, comma));

            nfa.addTransition(state[0], target[0], getId(alphabet, line.substring(comma + 1)));
            if (state[1] == 1) nfa.setAccepting(state[0], true);
            if (target[1] == 1) nfa.setAccepting(target[0], true);
            if (state[2] == 1) {
                nfa.initial = state[0];
            }
        }
        return nfa;
    }

    static int[] getState(String str) {
        str = str.trim();
        int isFinal = 0;
        int isInitial = 0;
        if (str.startsWith("(")) {
            isFinal = 1;
            str = str.substring(1, str.length() - 1);
        }
        else if (str.startsWith("i") || str.startsWith("I")) {
            isInitial = 1;
            str = str.substring(1);
        }
        if (str.startsWith("s") || str.startsWith("S")) {
            str = str.substring(1);
        }
        return new int[]{Integer.parseInt(str), isFinal, isInitial};
    }

    static int getId(Alphabet alphabet, String input) {
        //todo epsipn
        if (input.equals("eps")) {
            return -1;
        }
        StringNode node = new StringNode(input.trim());
        return alphabet.addRegex(node);
    }
}
