package mesut.parserx.dfa;

import mesut.parserx.nodes.StringNode;
import mesut.parserx.utils.IOUtils;

import java.io.*;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NfaReader {

    public static NFA read(File file) throws IOException {
        return read(IOUtils.read(new FileInputStream(file)));
    }

    //initial 0
    //final state list separated by space
    //state -> target, symbol
    //symbol type is string without quotes
    public static NFA read(String str) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(str));
        NFA nfa = new NFA(100);
        String line;
        Alphabet alphabet = new Alphabet();
        nfa.tree.alphabet = alphabet;
        boolean gotInitial = false;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("initial") || line.startsWith("start")) {
                gotInitial = true;
                Pattern p = Pattern.compile("(initial|start)\\s*=\\s*(.*)");
                Matcher m = p.matcher(line);
                m.find();
                nfa.initial = Integer.parseInt(m.group(2));
                continue;
            }
            if (line.startsWith("final")) {
                Pattern p = Pattern.compile("final\\s*=\\s*(.*)");
                Matcher m = p.matcher(line);
                m.find();
                for (String f : m.group(1).split(" ")) {
                    if (f.trim().isEmpty()) continue;
                    nfa.setAccepting(Integer.parseInt(f.trim()), true);
                }
                continue;
            }
            int arrow = line.indexOf("->");
            int comma = line.indexOf(",");
            if (arrow == -1) {
                throw new RuntimeException("invalid line: " + line + " '->' expected");
            }
            int state = Integer.parseInt(line.substring(0, arrow).trim());
            if (comma == -1) {
                //epsilon
                int target = Integer.parseInt(line.substring(arrow + 2).trim());
                nfa.addEpsilon(state, target);
            }
            else {
                //with input
                int target = Integer.parseInt(line.substring(arrow + 2, comma).trim());
                StringNode node = new StringNode(line.substring(comma + 1).trim());
                nfa.addTransition(state, target, alphabet.addRegex(node));
            }
        }
        if (!gotInitial) {
            throw new RemoteException("no initial state");
        }
        return nfa;
    }
}
