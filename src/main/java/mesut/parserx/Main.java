package mesut.parserx;

import mesut.parserx.dfa.Minimization;
import mesut.parserx.dfa.NFA;
import mesut.parserx.gen.Lang;
import mesut.parserx.gen.ast.AstGen;
import mesut.parserx.gen.lexer.LexerGenerator;
import mesut.parserx.gen.lldfa.ParserGen;
import mesut.parserx.gen.lldfa.RecursionHandler;
import mesut.parserx.gen.lr.LrCodeGen;
import mesut.parserx.gen.lr.LrType;
import mesut.parserx.gen.transform.EpsilonTrimmer;
import mesut.parserx.nodes.Node;
import mesut.parserx.nodes.NodeList;
import mesut.parserx.nodes.Tree;
import mesut.parserx.regex.RegexBuilder;
import mesut.parserx.utils.Utils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static List<String> cmds = Arrays.asList("-left", "-epsilon",
            "-optimize", "-dfa", "-nfa", "-nfa2dfa", "-regex", "-lldfa", "-lexer", "-lalr1", "-lr1");

    static String usageStr = "usage:\n" +
            "java -jar <jarfile> <command>\n" +
            "commands are;\n" +
            "-left                             left recursion removal\n" +
            "-epsilon                          epsilon removal\n" +
            "-optimize                         dfa optimization (input & output are in fsm format)\n" +
            "-dfa [-optimize] [-dot]           dfa from grammar\n" +
            "-nfa [-dot]                       nfa from grammar\n" +
            "-nfa2dfa [-optimize]              nfa to dfa\n" +
            "-regex                            nfa to regex\n" +
            "-lexer [-test] [-dump] [-out <path>] [-package <pkg>] [-lexerClass <cls>] [-lexerFunc <func>] [-tokenClass <cls>] generates just lexer\n" +
            "-lldfa [-test] [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates LL(1) recursive descent parser\n" +
            "-lalr1 [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates lalr(1) parser" +
            "-lr1 [-out <path>] [-package <pkg>] [-parserClass <cls>] [-astClass <cls>] [..lexer options] generates lr(1) parser" +
            "\ninput is given by '-in <path>' or as last argument" +
            "\noutput language is given by '-lang [java,cpp]'";

    static void printUsage() {
        System.err.println(usageStr);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        String cmd = args[0];
        if (!cmds.contains(cmd)) {
            System.err.println("invalid command '" + cmd + "'");
            printUsage();
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
        Lang lang = Lang.JAVA;
        boolean hasDot = false;
        boolean isTest = false;
        boolean dump = false;
        boolean optimize = false;
        for (int i = 1; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-input") || s.equals("-in")) {
                input = new File(args[i + 1]);
                i++;
            } else if (s.equals("-output") || s.equals("-out")) {
                output = new File(args[i + 1]);
                i++;
            } else if (s.equals("-test")) {
                isTest = true;
            } else if (s.equals("-dot")) {
                hasDot = true;
            } else if (s.equals("-dump")) {
                dump = true;
            } else if (s.equals("-package") || s.equals("-pkg")) {
                pkg = args[i + 1];
                i++;
            } else if (s.equals("-lexerClass")) {
                lexerClass = args[i + 1];
                i++;
            } else if (s.equals("-lexerFunc")) {
                lexerFunc = args[i + 1];
                i++;
            } else if (s.equals("-tokenClass")) {
                tokenClass = args[i + 1];
                i++;
            } else if (s.equals("-parserClass")) {
                parserClass = args[i + 1];
                i++;
            } else if (s.equals("-astClass")) {
                astClass = args[i + 1];
                i++;
            } else if (s.equals("-lang")) {
                lang = Lang.from(args[i + 1]);
                i++;
            } else if (s.equals("-optimize")) {
                optimize = true;
            } else if (i == args.length - 1) {
                //last arg might be input
                if (input == null) {
                    input = new File(s);
                }
            }
        }
        if (input == null) {
            System.err.println("provide an input file \ncmd is " + NodeList.join(Arrays.asList(args), " "));
            return;
        }
        try {
            switch (cmd) {
                case "-left": {
                    Tree tree = Tree.makeTree(input);
                    new AstGen(tree).gen();
                    new RecursionHandler(tree).handleAll();
                    RecursionHandler.clearArgs(tree);
                    if (output == null) {
                        output = new File(input.getAbsoluteFile().getParent(), Utils.noext(input.getName(), "-out.g"));
                    }
                    Utils.write(tree.toString(), output);
                    break;
                }
                case "-epsilon": {
                    Tree tree = Tree.makeTree(input);
                    if (output == null) {
                        output = new File(input.getAbsoluteFile().getParent(), Utils.noext(input.getName(), "-out.g"));
                    }
                    EpsilonTrimmer.trim(tree);
                    Utils.write(tree.toString(), output);
                    break;
                }
                case "-nfa": {
                    Tree tree = Tree.makeTree(input);
                    NFA nfa = tree.makeNFA();
                    if (output == null) {
                        output = new File(input.getParent(), Utils.noext(input.getName(), ".nfa"));
                    }
                    if (hasDot) {
                        File dotFile = new File(output.getParent(), Utils.noext(input.getName(), ".dot"));
                        nfa.dot(dotFile);
                        logwrite(dotFile);
                    }

                    nfa.dump(new PrintWriter(new FileWriter(output)));
                    logwrite(output);
                    break;
                }
                case "-dfa": {
                    //grammar -> dfa
                    Tree tree = Tree.makeTree(input);
                    NFA dfa = tree.makeNFA().dfa();
                    if (optimize) {
                        Minimization.removeDead(dfa);
                        Minimization.removeUnreachable(dfa);
                        Minimization.optimize(dfa);
                    }
                    if (output == null) {
                        output = new File(input.getParent(), Utils.noext(input.getName(), ".dfa"));
                    }
                    if (hasDot) {
                        File dotFile = new File(output.getParent(), Utils.noext(input.getName(), ".dot"));
                        dfa.dot(dotFile);
                        logwrite(dotFile);
                    }

                    dfa.dump(new PrintWriter(new FileWriter(output)));
                    logwrite(output);
                    break;
                }
                case "-regex": {
                    //nfa2regex
                    NFA nfa = NFA.read(input);
//                if (!Validator.isDFA(nfa)) {
//                    System.out.println("input is nfa converting to dfa first");
//                    nfa = nfa.dfa();
//                }
                    Node node = RegexBuilder.from(nfa);
                    if (output == null) {
                        System.out.println(node);
                    } else {
                        Utils.write(node.toString(), output);
                    }
                    break;
                }
                case "-nfa2dfa": {
                    NFA nfa = NFA.read(input);
                    NFA dfa = nfa.dfa();
                    if (optimize) {
                        dfa = Minimization.Hopcroft(dfa);
                        //dfa = Minimization.optimize(nfa);
                    }
                    if (output == null) {
                        output = new File(input.getParent(), Utils.noext(input.getName(), ".dfa"));
                    }
                    output = output.getAbsoluteFile();
                    if (hasDot) {
                        File nfaDot = new File(output.getParent(), Utils.noext(input.getName(), "-nfa.dot"));
                        nfa.dot(nfaDot);
                        logwrite(nfaDot);

                        File dfaDot = new File(output.getParent(), Utils.noext(input.getName(), "-dfa.dot"));
                        dfa.dot(dfaDot);
                        logwrite(dfaDot);
                    }
                    dfa.dump(new PrintWriter(new FileWriter(output)));
                    logwrite(output);
                    break;
                }
                case "-lexer": {
                    Tree tree = Tree.makeTree(input);
                    if (output == null) {
                        tree.options.outDir = input.getAbsoluteFile().getParent();
                    } else {
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
                    var generator = LexerGenerator.gen(tree, lang);
                    if (hasDot) {
                        generator.dfa.dot(Utils.noext(tree, ".dot"));
                    }
                    if (dump) {
                        generator.dfa.dump(new FileWriter(Utils.noext(tree, ".dfa")));
                    }
                    if (isTest) {
                        File tester = new File(tree.options.outDir, "LexerTester.java");
                        Utils.copyRes("LexerTester.java.template", Main.class.getClassLoader(), tester);
                        Utils.compile(tester, new File(tree.options.outDir, "out"));
                    }
                    break;
                }
                case "-lldfa": {
                    var tree = Tree.makeTree(input);
                    if (output == null) {
                        tree.options.outDir = input.getAbsoluteFile().getParent();
                    } else {
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
                    ParserGen.genCC(tree, lang);
                    if (isTest) {
                        File tester = new File(output, "ParserTester.java");
                        Utils.copyRes("ParserTester.java.template", Main.class.getClassLoader(), tester);
                        Utils.compile(tester, new File(tree.options.outDir, "out"));
                    }
                    break;
                }
                case "-lalr1":
                case "-lr1": {
                    Tree tree = Tree.makeTree(input);
                    if (output == null) {
                        tree.options.outDir = input.getParent();
                    } else {
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
                    LrType type;
                    if (cmd.equals("-lr1")) {
                        type = LrType.LR1;
                    } else {
                        type = LrType.LALR1;
                    }
                    LrCodeGen gen = new LrCodeGen(tree, type);
                    gen.gen();
                    if (hasDot) {
                        File dotFile = new File(tree.options.outDir, Utils.noext(input.getName(), "-dfa.dot"));
                        gen.gen.writeDot(new PrintWriter(dotFile));
                        File table = new File(tree.options.outDir, Utils.noext(input.getName(), "-table.dot"));
                        gen.gen.writeTableDot(new PrintWriter(table));
                    }
                    break;
                }
                default:
                    throw new Exception("unknown command: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logwrite(File file) {
        System.out.println("writing " + file);
    }

    public static URL getResStream(String name) throws IOException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        return Main.class.getResource(name);
    }

    public static File getResFile(String name) throws IOException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        URL url = Main.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(name + " not found");
        }
        return new File(url.getPath());
    }
}
