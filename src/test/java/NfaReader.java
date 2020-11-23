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
        boolean inState = false;
        int state = -1;
        Alphabet alphabet = new Alphabet();
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            if (inState) {
                String[] arr = line.split("->");
                int target = getState(arr[1]);
                nfa.addTransition(state, target, getId(alphabet, arr[0].trim()));
            }
            else {
                state = getState(line);
                inState = true;
            }
        }
        return nfa;
    }

    static int getState(String line) throws IOException {
        if (line.startsWith("s") || line.startsWith("S")) {
            return Integer.parseInt(line.substring(1));
        }
        else {
            throw new IOException("invalid line:" + line);
        }
    }

    static int getId(Alphabet alphabet, String input) {
        StringNode node = new StringNode(input);
        return alphabet.addRegex(node);
    }
}
