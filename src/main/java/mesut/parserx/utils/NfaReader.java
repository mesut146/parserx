package mesut.parserx.utils;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.DFA;
import mesut.parserx.dfa.NFA;
import mesut.parserx.nodes.StringNode;

import java.io.*;

public class NfaReader {

    public static NFA read(File file) throws IOException {
        return read(Helper.read(new FileInputStream(file)));
    }

    //%state -> target, symbol
    //state -> (final), symbol
    //symbol type is string without quotes
    public static NFA read(String str) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(str));
        NFA nfa = new DFA(100);
        String line;
        Alphabet alphabet = new Alphabet();
        nfa.tree.alphabet = alphabet;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            int arrow = line.indexOf("->");
            int comma = line.indexOf(",");
            if (arrow == -1 || comma == -1) {
                throw new RuntimeException("invalid line: " + line + " '->' and ',' expected");
            }
            int[] state = getState(line.substring(0, arrow));
            int[] target = getState(line.substring(arrow + 2, comma));

            int id = getId(alphabet, line.substring(comma + 1));
            if (id == -1) {
                nfa.addEpsilon(state[0], target[0]);
            }
            else {
                nfa.addTransition(state[0], target[0], id);
            }
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
        else if (str.startsWith("%")) {
            isInitial = 1;
            str = str.substring(1);
        }
        if (str.startsWith("s") || str.startsWith("S")) {
            str = str.substring(1);
        }
        return new int[]{Integer.parseInt(str), isFinal, isInitial};
    }

    static int getId(Alphabet alphabet, String input) {
        //todo epsilon
        if (input.equalsIgnoreCase("eps") || input.equalsIgnoreCase("epsilon")) {
            return -1;
        }
        StringNode node = new StringNode(input.trim());
        return alphabet.addRegex(node);
    }
}
