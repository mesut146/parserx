package nodes;

import dfa.Alphabet;
import dfa.NFA;
import grammar.GParser;
import grammar.ParseException;
import rule.RuleDecl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//the grammar file for both lexer and parser
public class Tree {

    public List<RuleDecl> rules;
    public NameNode start;
    public File file = null;
    List<TokenDecl> tokens;
    List<File> includes;
    public Alphabet alphabet = new Alphabet();

    public Tree() {
        tokens = new ArrayList<>();
        rules = new ArrayList<>();
        includes = new ArrayList<>();
    }

    public Tree(Tree tree) {
        this();
        start = tree.start;
        includes = tree.includes;
        file = tree.file;
        tokens = tree.tokens;
    }

    public static Tree makeTree(File path) {
        try {
            GParser parser = new GParser(new FileReader(path));
            return parser.tree(path);
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static int indexOf(List<TokenDecl> list, String name) {
        int i = 0;
        for (TokenDecl decl : list) {
            if (decl.tokenName.equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }


    //merge two grammar files(lexer,parser)
    void mergeWith(Tree other) {
        tokens.addAll(other.tokens);
        rules.addAll(other.rules);
    }

    public void addInclude(String path) {
        if (file != null) {
            File refFile = new File(file.getParent(), path);
            if (!refFile.exists()) {
                throw new IllegalArgumentException("grammar file " + path + " not found");
            }
            Tree other = makeTree(refFile);
            mergeWith(other);
            includes.add(refFile);
        }
        else {
            throw new IllegalArgumentException("grammar file " + path + " not found");
        }
    }

    public void addToken(TokenDecl token) {
        tokens.add(token);
    }

    public void addSkip(TokenDecl token) {
        token.isSkip = true;
        addToken(token);
    }

    public void addRule(RuleDecl rule) {
        rule.index = rules.size();
        rules.add(rule);
    }

    public boolean hasRule(String name) {
        for (RuleDecl decl : rules) {
            if (decl.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    //find token by string literal
    public TokenDecl getTokenByValue(String val) {
        for (TokenDecl decl : tokens) {
            if (decl.regex.isString() && decl.regex.asString().value.equals(val)) {
                return decl;
            }
        }
        return null;
    }

    public TokenDecl getToken(String name) {
        int idx = indexOf(name);
        if (idx == -1) {
            return null;
        }
        return tokens.get(idx);
    }

    //get index of token by name
    public int indexOf(String name) {
        return indexOf(tokens, name);
    }

    //construct NFA from this grammar file
    public NFA makeNFA() {
        makeDistinctRanges();
        NFA nfa = new NFA(100);
        nfa.tree = this;
        for (TokenDecl decl : tokens) {
            if (!decl.fragment) {
                nfa.addRegex(decl);
            }
        }
        return nfa;
    }

    void printTokens(StringBuilder sb, List<TokenDecl> list, String title) {
        if (!list.isEmpty()) {
            sb.append(title);
            sb.append("{\n");
            for (TokenDecl td : list) {
                sb.append("  ");
                sb.append(td);
                sb.append(";\n");
            }
            sb.append("}");
            sb.append("\n\n");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<TokenDecl> normal = new ArrayList<>();
        List<TokenDecl> skips = new ArrayList<>();
        for (TokenDecl tokenDecl : tokens) {
            if (tokenDecl.isSkip) {
                skips.add(tokenDecl);
            }
            else {
                normal.add(tokenDecl);
            }
        }
        printTokens(sb, normal, "token");
        printTokens(sb, skips, "skip");

        if (!rules.isEmpty()) {
            sb.append("/* rules */\n");
            if (start != null) {
                sb.append("@start = ").append(start).append(";\n\n");
            }
            sb.append(NodeList.join(rules, "\n"));
        }

        return sb.toString();
    }

    //find all intersecting inputs and split them so that all of them becomes unique
    private void makeDistinctRanges() {
        Set<RangeNode> ranges = new HashSet<>();//whole input set as ranges nodes
        List<Bracket> brackets = new ArrayList<>();
        for (TokenDecl decl : tokens) {
            walkNodes(decl.regex, ranges, brackets);
        }
        //find intersecting ranges and split them
        outer:
        while (true) {
            for (Bracket b : brackets) {
                for (RangeNode rangeNode : b.rangeNodes) {
                    //if this range intersect other ranges
                    for (RangeNode otherRange : ranges) {
                        if (rangeNode.isSingle() && rangeNode.intersect(otherRange) && !rangeNode.same(otherRange)) {
                            RangeNode inter = RangeNode.intersect(rangeNode, otherRange);
                            RangeNode me1 = RangeNode.of(rangeNode.start, inter.start - 1);
                            RangeNode me2 = RangeNode.of(inter.end + 1, rangeNode.end);
                            RangeNode he1 = RangeNode.of(otherRange.start, inter.start - 1);
                            RangeNode he2 = RangeNode.of(inter.end + 1, otherRange.end);
                            b.rangeNodes.remove(rangeNode);
                            ranges.remove(rangeNode);
                            if (me1.isValid()) {
                                b.rangeNodes.add(me1);
                                ranges.add(me1);
                            }
                            if (me2.isValid()) {
                                b.rangeNodes.add(me2);
                                ranges.add(me2);
                            }
                            ranges.remove(otherRange);
                            if (he1.isValid()) ranges.add(he1);
                            if (he2.isValid()) ranges.add(he2);

                            b.rangeNodes.add(inter);
                            ranges.add(inter);
                            b.clear();
                            b.addAll(b.rangeNodes);
                            //start from zero to check if newly added ranges breaks existing ones
                            continue outer;
                        }//for ranges
                    }//for ranges
                }//for bracket
            }
            break;//found none break while
        }//while
        for (RangeNode rangeNode : ranges) {
            alphabet.add(rangeNode);
        }
    }

    static void walkNodes(Node node, Set<RangeNode> ranges, List<Bracket> brackets) {
        //find all ranges and store them
        if (node.isBracket()) {
            Bracket b = node.asBracket();
            ranges.addAll(b.normalize().rangeNodes);
            brackets.add(b);
        }
        else if (node.isSequence()) {
            for (Node c : node.asSequence()) {
                walkNodes(c, ranges, brackets);
            }
        }
        else if (node.isString()) {
            StringNode stringNode = node.asString();
            if (stringNode.isDot) {
                Bracket b = stringNode.toBracket().normalize();
                ranges.addAll(b.rangeNodes);
                brackets.add(b);
            }
            else {
                //make range for each char in string
                String str = node.asString().value;
                for (char c : str.toCharArray()) {
                    ranges.add(RangeNode.of(c, c));
                }
            }

        }
        else if (node.isGroup()) {
            walkNodes(node.asGroup().rhs, ranges, brackets);
        }
        else if (node.isRegex()) {
            walkNodes(node.asRegex().node, ranges, brackets);
        }
        else if (node.isOr()) {
            for (Node c : node.asOr()) {
                walkNodes(c, ranges, brackets);
            }
        }
    }

    public RuleDecl getRule(String name) {
        List<RuleDecl> list = getRules(name);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<RuleDecl> getRules(String name) {
        List<RuleDecl> list = new ArrayList<>();
        for (RuleDecl decl : rules) {
            if (decl.name.equals(name)) {
                list.add(decl);
            }
        }
        return list;
    }
}
