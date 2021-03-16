package nodes;

import dfa.Alphabet;
import dfa.CharClass;
import dfa.NFA;
import gen.PrepareTree;
import grammar.GParser;
import grammar.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

//the grammar file for both lexer and parser
public class Tree {

    public List<RuleDecl> rules;
    public NameNode start;
    public File file = null;
    public Alphabet alphabet = new Alphabet();
    List<TokenDecl> tokens;
    List<File> includes;

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
            return parser.tree(path).prepare();
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

    public Tree prepare() {
        return PrepareTree.checkReferences(this);
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
        rule.tree = this;
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
        CharClass.makeDistinctRanges(this);
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

    public List<TokenDecl> getTokens() {
        return tokens;
    }

}
