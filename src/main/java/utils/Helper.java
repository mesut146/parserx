package utils;

import dfa.DFA;
import dfa.NFA;
import grammar.ParseException;
import nodes.Tree;

import java.io.File;
import java.util.List;

public class Helper {
    public static NFA makeNFA(File path) throws ParseException {
        return Tree.makeTree(path).makeNFA();
    }

    public static DFA makeDFA(File path) throws ParseException {
        return Tree.makeTree(path).makeNFA().dfa();
    }

    public static <T> String join(List<T> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }
}
