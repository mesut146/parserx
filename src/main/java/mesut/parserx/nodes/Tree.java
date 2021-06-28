package mesut.parserx.nodes;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.grammar.GParser;
import mesut.parserx.utils.IOUtils;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

//the grammar file for both lexer and parser
public class Tree {

    public List<RuleDecl> rules;
    public List<RuleDecl> hiddenRules;
    public Name start;
    public File file = null;
    public Alphabet alphabet = new Alphabet();
    public List<TokenDecl> tokens;
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
            String grammar = IOUtils.read(path);
            grammar += " ";
            GParser parser = new GParser(new StringReader(grammar));
            return parser.tree(path).prepare();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Tree makeTree(String grammar) {
        grammar += " ";
        try {
            GParser parser = new GParser(new StringReader(grammar));
            return parser.tree(null).prepare();
        } catch (Exception e) {
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
        PrepareTree.checkReferences(this);
        return this;
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
        if (indexOf(token.tokenName) != -1) {
            throw new RuntimeException("token " + token + " already exists");
        }
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
        return NFABuilder.build(this);
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
        if (!hiddenRules.isEmpty()) {
            sb.append("/* hidden rules */\n");
            sb.append(NodeList.join(hiddenRules, "\n"));
        }

        return sb.toString();
    }

    public RuleDecl getRule(String name) {
        for (RuleDecl decl : rules) {
            if (decl.name.equals(name)) return decl;
        }
        return null;
    }

    public RuleDecl getRule(Name res) {
        for (RuleDecl decl : rules) {
            if (decl.ref().equals(res)) {
                return decl;
            }
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
