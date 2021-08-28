package mesut.parserx;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NfaReader;
import mesut.parserx.dfa.Validator;
import mesut.parserx.gen.Factor;
import mesut.parserx.gen.LeftRecursive;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static List<String> cmds = Arrays.asList("-left", "-factor", "-epsilon", "-optimize", "-dfa", "-nfa", "-nfa2dfa", "-regex", "-lr0");

    static String usageStr = "usage:\n" +
            "java -jar <jarfile> <command>\n" +
            "commands are;\n" +
            "-left                             left recursion removal\n" +
            "-factor                           left factoring\n" +
            "-epsilon                          epsilon removal\n" +
            "-optimize                         dfa optimization (input & output are in fsm format)\n" +
            "-dfa [-optimize] [-dot <dotfile>] dfa from grammar\n" +
            "-nfa [-dot <dotfile>]             nfa from grammar\n" +
            "-nfa2dfa [-optimize]              nfa to dfa\n" +
            "-regex                            nfa to regex";

    static void usage() {
        System.err.println(usageStr);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        File input = null;
        File output = null;
        List<String> cmd = new ArrayList<>();
        File dot = null;
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-input") || s.equals("-in")) {
                input = new File(args[i + 1]);
                i++;
            }
            else if (s.equals("-output") || s.equals("-out")) {
                output = new File(args[i + 1]);
                i++;
            }
            else if (s.equals("-dot")) {
                dot = new File(args[i + 1]);
                i++;
            }
            else if (cmds.contains(s)) {
                cmd.add(s);
            }
        }
        if (cmd.isEmpty()) {
            System.err.println("provide a valid command");
            System.err.println("valid commands are= " + cmds);
            return;
        }
        if (input == null) {
            System.err.println("provide an input file \ncmd is " + NodeList.join(Arrays.asList(args), " "));
            return;
        }
        try {
            if (cmd.contains("-left")) {
                Tree tree = Tree.makeTree(input);
                tree = LeftRecursive.transform(tree);
                if (output == null) {
                    output = new File(tree.file + "-out.g");
                }
                Utils.write(tree.toString(), output);
                logwrite(output);
            }
            else if (cmd.contains("-factor")) {
                Tree tree = Tree.makeTree(input);
                Factor.keepFactor = false;
                new Factor(tree).handle();
                if (output == null) {
                    output = new File(tree.file + "-out.g");
                }
                Utils.write(tree.toString(), output);
                logwrite(output);
            }
            else if (cmd.contains("-nfa")) {
                Tree tree = Tree.makeTree(input);
                NFA nfa = tree.makeNFA();
                if (dot != null) {
                    nfa.dot(new FileWriter(dot));
                    logwrite(dot);
                }
                if (output == null) {
                    output = new File(input + ".nfa");
                }
                nfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else if (cmd.contains("-dfa")) {
                //g2dfa
                Tree tree = Tree.makeTree(input);
                NFA dfa = tree.makeNFA().dfa();
                if (cmd.contains("-optimize")) {
                    Minimization.removeDead(dfa);
                    Minimization.removeUnreachable(dfa);
                    Minimization.optimize(dfa);
                }
                if (dot != null) {
                    dfa.dot(new FileWriter(dot));
                    logwrite(dot);
                }
                if (output == null) {
                    output = new File(input + ".dfa");
                }
                dfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else if (cmd.contains("-regex")) {
                //nfa2regex
                NFA nfa = NfaReader.read(input);
                if (!Validator.isDFA(nfa)) {
                    System.out.println("inout is nfa converting to nfa first");
                    nfa = nfa.dfa();
                }
                Node node = RegexBuilder.from(nfa);
                if (output == null) {
                    System.out.println(node);
                }
                else {
                    Utils.write(node.toString(), output);
                    logwrite(output);
                }
            }
            else if (cmd.contains("-nfa2dfa")) {
                NFA nfa = NfaReader.read(input);
                NFA dfa = nfa.dfa();
                if (cmd.contains("-optimize")) {
                    dfa = Minimization.Hopcroft(dfa);
                    //dfa = Minimization.optimize(nfa);
                }
                if (dot != null) {
                    File nfaDot = new File(dot.getParent(), input.getName() + "-nfa.dot");
                    nfa.dot(new PrintWriter(new FileWriter(nfaDot)));
                    logwrite(nfaDot);
                    dfa.dot(new PrintWriter(new FileWriter(dot)));
                    logwrite(dot);
                }
                if (output == null) {
                    output = new File(input + ".dfa");
                }
                dfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else {
                throw new Exception("unknown commands: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void logwrite(File file) {
        System.out.println("writing " + file);
    }

}
