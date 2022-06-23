package mesut.parserx.nodes;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.parser.AstBuilder;
import mesut.parserx.utils.CountingMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

//the grammar file for both lexer and parser
public class Tree {

    public List<TokenDecl> tokens = new ArrayList<>();
    public List<RuleDecl> rules = new ArrayList<>();
    public Options options = new Options();
    public Name start;
    public File file;
    public Alphabet alphabet = new Alphabet();
    List<String> includes = new ArrayList<>();
    CountingMap<String> newNameCnt = new CountingMap<>();
    Map<String, String> senderMap = new HashMap<>();
    HashSet<Name> originalRules = new HashSet<>();
    public boolean checkDup = true;

    public Tree() {
    }

    public Tree(Tree tree) {
        this();
        start = tree.start;
        includes = tree.includes;
        file = tree.file;
        tokens = tree.tokens;
        options = tree.options;
        originalRules = tree.originalRules;
    }

    public static Tree makeTree(File path) {
        try {
            return AstBuilder.makeTree(path).prepare();
        } catch (Exception e) {
            e.addSuppressed(new RuntimeException(path.getAbsolutePath()));
            throw new RuntimeException(e);
        }
    }

    static int indexOf(List<TokenDecl> list, String name) {
        int i = 0;
        for (TokenDecl decl : list) {
            if (decl.name.equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public Tree prepare() {
        PrepareTree.checkReferences(this);
        for (RuleDecl decl : rules) {
            originalRules.add(decl.ref);
        }
        return this;
    }

    public boolean isOriginal(Name name) {
        return originalRules.contains(name);
    }

    //merge two grammar files(lexer,parser)
    void mergeWith(Tree other) {
        for (TokenDecl decl : other.tokens) {
            addToken(decl);
        }
        for (RuleDecl decl : other.rules) {
            addRule(decl);
        }
    }

    public void addInclude(String path) {
        includes.add(path);
    }

    public void resolveIncludes() throws FileNotFoundException {
        for (String path : includes) {
            File abs = new File(path);
            if (abs.exists()) {
                Tree other = makeTree(abs);
                mergeWith(other);
                return;
            }
            else if (file != null) {
                //relative
                File refFile = new File(file.getParent(), path);
                if (refFile.exists()) {
                    Tree other = makeTree(refFile);
                    mergeWith(other);
                    return;
                }
            }
            throw new FileNotFoundException("grammar file " + path + " not found");
        }
    }

    public void addToken(TokenDecl token) {
        if (indexOf(token.name) != -1) {
            throw new RuntimeException("token " + token + " already exists");
        }
        tokens.add(token);
    }

    public void addSkip(TokenDecl token) {
        token.isSkip = true;
        addToken(token);
    }

    public void addRule(RuleDecl rule) {
        if (checkDup) {
            for (RuleDecl old : rules) {
                if (old.ref.equals(rule.ref)) {
                    throw new RuntimeException("duplicate rule");
                }
            }
        }
        rule.index = rules.size();
        rules.add(rule);
    }

    //is it safe to use name
    public String getFreeName(String name) {
        String cur = name;
        while (true) {
            if (getRule(cur) == null) {
                senderMap.put(cur, name);
                return cur;
            }
            int cnt = newNameCnt.get(name);
            cur = name + cnt;
        }
    }

    //find root of rule
    public String getSender(String name) {
        if (senderMap.containsKey(name)) {
            return senderMap.get(name);
        }
        else {
            return name;
        }
    }

    public Name getFactorOne(Name old, Name factor) {
        Name res = new Name(old.name + "_" + factor.name);
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        res.args.add(factor.copy());
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public Name getFactorZero(Name old, Name factor) {
        Name res = new Name(old.name + "_no_" + factor.name);
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public Name getFactorPlusZero(Name old, Regex factor) {
        Name res = new Name(old.name + "_nop_" + factor.node.asName().name);
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public Name getFactorPlusOne(Name old, Regex factor) {
        Name res = new Name(getFreeName(getSender(old.name)));
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        res.args.add(factor.copy());
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public Name getEps(Name old) {
        Name res = new Name(old.name + "_eps");
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public Name getNoEps(Name old) {
        Name res = new Name(old.name + "_noe");
        res.astInfo = old.astInfo.copy();
        res.args = new ArrayList<>(old.args);
        senderMap.put(res.name, getSender(old.name));
        return res;
    }

    public void addRuleBelow(RuleDecl rule, RuleDecl prev) {
        for (RuleDecl old : rules) {
            if (old.ref.equals(rule.ref)) {
                throw new RuntimeException("wtf");
            }
        }
        rule.index = rules.size();
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i) == prev) {
                rules.add(i + 1, rule);
                return;
            }
        }
    }

    boolean isStr(Node node, String str) {
        if (node.isSequence()) {
            node = node.asSequence().normal();
        }
        return node.isString() && node.asString().value.equals(str);
    }

    //find token by string literal
    public TokenDecl getTokenByValue(String val) {
        for (TokenDecl decl : tokens) {
            if (isStr(decl.rhs, val)) return decl;
            else if (decl.rhs.isOr()) {
                for (Node ch : decl.rhs.asOr()) {
                    if (isStr(ch, val)) {
                        return decl;
                    }
                }
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
                sb.append("%start = ").append(start).append(";\n\n");
            }
            sb.append(NodeList.join(rules, "\n"));
        }
        return sb.toString();
    }

    public RuleDecl getRule(String name) {
        return getRule(new Name(name));
    }

    public RuleDecl getRule(Name name) {
        for (RuleDecl decl : rules) {
            if (decl.ref.equals(name)) return decl;
        }
        return null;
    }

    public List<RuleDecl> getRules(Name name) {
        List<RuleDecl> list = new ArrayList<>();
        for (RuleDecl decl : rules) {
            if (decl.ref.equals(name)) {
                list.add(decl);
            }
        }
        return list;
    }

    public Tree revert() {
        Helper.revert(this);
        return this;
    }

    //todo debug
    public void printRules() {
        System.out.println("---------------------");
        System.out.println(NodeList.join(rules, "\n"));
        System.out.println("---------------------");
    }
}
