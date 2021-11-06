package mesut.parserx;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NfaReader;
import mesut.parserx.dfa.Validator;
import mesut.parserx.gen.LexerGenerator;
import mesut.parserx.gen.ll.RecDescent;
import mesut.parserx.gen.lr.AstBuilderGen;
import mesut.parserx.gen.lr.CodeGen;
import mesut.parserx.gen.transform.Factor;
import mesut.parserx.gen.transform.LeftRecursive;
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

    static List<String> cmds = Arrays.asList("-left", "-factor", "-epsilon",
            "-optimize", "-dfa", "-nfa", "-nfa2dfa", "-regex", "-lr0", "-desc", "-lexer", "-lalr", "-lr1", "-lr0");

    static String usageStr = "usage:\n" +
            "java -jar <jarfile> <command>\n" +
            "commands are;\n" +
            "-left                             left recursion removal\n" +
            "-factor                           left factoring\n" +
            "-epsilon                          epsilon removal\n" +
            "-optimize                         dfa optimization (input & output are in fsm format)\n" +
            "-dfa [-optimize] [-dot]           dfa from grammar\n" +
            "-nfa [-dot]                       nfa from grammar\n" +
            "-nfa2dfa [-optimize]              nfa to dfa\n" +
            "-regex                            nfa to regex\n" +
            "-lexer [-out <path>] [-package <pkg>] [-lexerClass <cls>] [-lexerFunc <func>] [-tokenClass <cls>]  generates just lexer\n" +
            "-desc [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates LL(1) recursive descent parser\n" +
            "-lalr [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates lalr parser" +
            "-lr1 [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates lr(1) parser" +
            "-lr0 [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates lr(0) parser" +
            "\ninput is given by -in <path> or as last argument";

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
        String pkg = null;
        String lexerClass = null;
        String lexerFunc = null;
        String tokenClass = null;
        String parserClass = null;
        String astClass = null;
        List<String> cmd = new ArrayList<>();
        boolean hasDot = false;
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
                hasDot = true;
            }
            else if (s.equals("-package") || s.equals("-pkg")) {
                pkg = args[i + 1];
                i++;
            }
            else if (s.equals("-lexerClass")) {
                lexerClass = args[i + 1];
                i++;
            }
            else if (s.equals("-lexerFunc")) {
                lexerFunc = args[i + 1];
                i++;
            }
            else if (s.equals("-tokenClass")) {
                tokenClass = args[i + 1];
                i++;
            }
            else if (s.equals("-parserClass")) {
                parserClass = args[i + 1];
                i++;
            }
            else if (s.equals("-astClass")) {
                astClass = args[i + 1];
                i++;
            }
            else if (cmds.contains(s)) {
                cmd.add(s);
            }
            else if (i == args.length - 1) {
                //last arg might be input
                if (input == null) {
                    input = new File(s);
                }
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
                    output = new File(input.getParent(), Utils.newName(input.getName(), "-out.g"));
                }
                Utils.write(tree.toString(), output);
            }
            else if (cmd.contains("-factor")) {
                Tree tree = Tree.makeTree(input);
                Factor.keepFactor = false;
                new Factor(tree).factorize();
                if (output == null) {
                    output = new File(input.getParent(), Utils.newName(input.getName(), "-out.g"));
                }
                Utils.write(tree.toString(), output);
            }
            else if (cmd.contains("-nfa")) {
                Tree tree = Tree.makeTree(input);
                NFA nfa = tree.makeNFA();
                if (output == null) {
                    output = new File(input.getParent(), Utils.newName(input.getName(), "nfa"));
                }
                if (hasDot) {
                    File dotFile = new File(output.getParent(), Utils.newName(input.getName(), "dot"));
                    nfa.dot(new FileWriter(dotFile));
                    logwrite(dotFile);
                }

                nfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else if (cmd.contains("-dfa")) {
                //grammar -> dfa
                Tree tree = Tree.makeTree(input);
                NFA dfa = tree.makeNFA().dfa();
                if (cmd.contains("-optimize")) {
                    Minimization.removeDead(dfa);
                    Minimization.removeUnreachable(dfa);
                    Minimization.optimize(dfa);
                }
                if (output == null) {
                    output = new File(input.getParent(), Utils.newName(input.getName(), "dfa"));
                }
                if (hasDot) {
                    File dotFile = new File(output.getParent(), Utils.newName(input.getName(), "dot"));
                    dfa.dot(new FileWriter(dotFile));
                    logwrite(dotFile);
                }

                dfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else if (cmd.contains("-regex")) {
                //nfa2regex
                NFA nfa = NfaReader.read(input);
                if (!Validator.isDFA(nfa)) {
                    System.out.println("input is nfa converting to nfa first");
                    nfa = nfa.dfa();
                }
                Node node = RegexBuilder.from(nfa);
                if (output == null) {
                    System.out.println(node);
                }
                else {
                    Utils.write(node.toString(), output);
                }
            }
            else if (cmd.contains("-nfa2dfa")) {
                NFA nfa = NfaReader.read(input);
                NFA dfa = nfa.dfa();
                if (cmd.contains("-optimize")) {
                    dfa = Minimization.Hopcroft(dfa);
                    //dfa = Minimization.optimize(nfa);
                }
                if (output == null) {
                    output = new File(input.getParent(), Utils.newName(input.getName(), "dfa"));
                }
                if (hasDot) {
                    File nfaDot = new File(output.getParent(), Utils.newName(input.getName(), "-nfa.dot"));
                    nfa.dot(new PrintWriter(new FileWriter(nfaDot)));
                    logwrite(nfaDot);

                    File dfaDot = new File(output.getParent(), Utils.newName(input.getName(), "-dfa.dot"));
                    dfa.dot(new PrintWriter(new FileWriter(dfaDot)));
                    logwrite(dfaDot);
                }
                dfa.dump(new PrintWriter(new FileWriter(output)));
                logwrite(output);
            }
            else if (cmd.contains("-lexer")) {
                Tree tree = Tree.makeTree(input);
                if (output == null) {
                    tree.options.outDir = input.getParent();
                }
                else {
                    tree.options.outDir = output.getAbsolutePath();
                }
                if (pkg != null) {
                    tree.options.packageName = pkg;
                }
                if (lexerClass != null) {
                    tree.options.lexerClass = lexerClass;
                }
                if (lexerFunc != null) {
                    tree.options.lexerFunction = lexerFunc;
                }
                if (tokenClass != null) {
                    tree.options.tokenClass = tokenClass;
                }
                LexerGenerator generator = new LexerGenerator(tree);
                generator.generate();
                if (hasDot) {
                    generator.dfa.dot(new FileWriter(new File(tree.options.outDir, Utils.newName(input.getName(), "dot"))));
                }
            }
            else if (cmd.contains("-desc")) {
                Tree tree = Tree.makeTree(input);
                if (output == null) {
                    tree.options.outDir = input.getParent();
                }
                else {
                    tree.options.outDir = output.getAbsolutePath();
                }
                if (pkg != null) {
                    tree.options.packageName = pkg;
                }
                if (lexerClass != null) {
                    tree.options.lexerClass = lexerClass;
                }
                if (lexerFunc != null) {
                    tree.options.lexerFunction = lexerFunc;
                }
                if (tokenClass != null) {
                    tree.options.tokenClass = tokenClass;
                }
                if (parserClass != null) {
                    tree.options.parserClass = parserClass;
                }
                if (astClass != null) {
                    tree.options.astClass = astClass;
                }
                RecDescent gen = new RecDescent(tree);
                gen.gen();
            }
            else if (cmd.contains("-lalr") || cmd.contains("-lr1") || cmd.contains("-lr0")) {
                Tree tree = Tree.makeTree(input);
                if (output == null) {
                    tree.options.outDir = input.getParent();
                }
                else {
                    tree.options.outDir = output.getAbsolutePath();
                }
                if (pkg != null) {
                    tree.options.packageName = pkg;
                }
                if (lexerClass != null) {
                    tree.options.lexerClass = lexerClass;
                }
                if (lexerFunc != null) {
                    tree.options.lexerFunction = lexerFunc;
                }
                if (tokenClass != null) {
                    tree.options.tokenClass = tokenClass;
                }
                if (parserClass != null) {
                    tree.options.parserClass = parserClass;
                }
                if (astClass != null) {
                    tree.options.astClass = astClass;
                }
                String type;
                if (cmd.contains("-lr1")) {
                    type = "lr1";
                }
                else if (cmd.contains("-lalr")) {
                    type = "lalr";
                }
                else {
                    type = "lr0";
                }
                CodeGen gen = new CodeGen(tree, type);
                gen.gen();
                AstBuilderGen builderGen = new AstBuilderGen(tree);
                builderGen.gen();
                if (hasDot) {
                    File dotFile = new File(tree.options.outDir, Utils.newName(input.getName(), "-dfa.dot"));
                    gen.gen.writeDot(new PrintWriter(dotFile));
                    File table = new File(tree.options.outDir, Utils.newName(input.getName(), "-table.dot"));
                    gen.gen.writeTableDot(new PrintWriter(table));
                }
            }
            else {
                throw new Exception("unknown commands: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logwrite(File file) {
        System.out.println("writing " + file);
    }


}
