package mesut.parserx;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.LeftRecursive;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.utils.Helper;
import mesut.parserx.utils.NfaReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static List<String> cmds = Arrays.asList("-left", "-epsilon", "-optimize", "-dfa", "-nfa", "nfa2dfa", "-regex", "lr0");

    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        File input = null;
        File output = null;
        List<String> cmd = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-input") || args[i].equals("-in")) {
                input = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-output") || args[i].equals("-out")) {
                output = new File(args[i + 1]);
                i++;
            }
            else if (cmds.contains(args[i])) {
                cmd.add(args[i]);
                i++;
            }
        }
        if (cmd.isEmpty()) {
            System.err.println("provide a valid command");
            System.err.println("valid commands are= " + cmds);
            return;
        }
        if (input == null) {
            System.err.println("provide a input file");
            return;
        }
        try {
            if (cmd.contains("-left")) {
                Tree tree = Tree.makeTree(input);
                tree = LeftRecursive.transform(tree);
                save(tree, output);
            }
            else if (cmd.contains("-nfa")) {
                Tree tree = Tree.makeTree(input);
                NFA nfa = tree.makeNFA();
                if (output == null) {
                    output = new File(input + "-nfa.dot");
                }
                nfa.dot(new FileWriter(output));
            }
            else if (cmd.contains("-dfa")) {
                Tree tree = Tree.makeTree(input);
                NFA dfa = tree.makeNFA().dfa();
                if (cmd.contains("-optimize")) {
                    Minimization.removeDead(dfa);
                    Minimization.removeUnreachable(dfa);
                    Minimization.optimize(dfa);
                }
                if (output == null) {
                    output = new File(input + "-dfa.dot");
                }
                dfa.dot(new FileWriter(output));
            }
            else if (cmd.contains("-regex")) {
                NFA nfa = NfaReader.read(input);
                Node node = RegexBuilder.from(nfa);
                if (output == null) {
                    System.out.println(node);
                }
                else {
                    Helper.write(node.toString(), output);
                }
            }
            else if (cmd.contains("-nfa2dfa")) {
                NFA dfa = NfaReader.read(input).dfa();
                if (cmd.contains("-optimize")) {
                    dfa = Minimization.Hopcroft(dfa);
                }
                if (output == null) {
                    output = new File(input + ".dfa");
                }
                dfa.dump(new PrintWriter(new FileWriter(output)));
            }
            else {
                throw new Exception("unknown commands: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void save(Tree tree, File output) {
        if (output == null) {
            output = new File(tree.file.getAbsolutePath() + "-out.g");
        }
        try {
            Helper.write(tree.toString(), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
