package utils;

import dfa.DFA;
import dfa.NFA;
import grammar.ParseException;
import nodes.Tree;

import java.io.*;

public class Helper {
    public static NFA makeNFA(File path) throws ParseException {
        return Tree.makeTree(path).makeNFA();
    }

    public static DFA makeDFA(File path) throws ParseException {
        return Tree.makeTree(path).makeNFA().dfa();
    }

    public static String read(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
