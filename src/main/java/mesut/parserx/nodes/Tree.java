package mesut.parserx.nodes;

import mesut.parserx.dfa.Alphabet;
import mesut.parserx.dfa.NFA;
import mesut.parserx.dfa.NFABuilder;
import mesut.parserx.gen.FirstSet;
import mesut.parserx.gen.Helper;
import mesut.parserx.gen.Options;
import mesut.parserx.gen.PrepareTree;
import mesut.parserx.parser.AstVisitor;
import mesut.parserx.utils.CountingMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

//the grammar file for both lexer and parser
public class Tree {

    public List<String> includes = new ArrayList<>();
    public Options options = new Options();
    public List<TokenBlock> tokenBlocks = new ArrayList<>();
    public LexerMembers lexerMembers;
    public List<RuleDecl> rules = new ArrayList<>();
    public Name start;
    public File file;
    public Alphabet alphabet = new Alphabet();
    CountingMap<String> newNameCnt = new CountingMap<>();
    Map<String, String> senderMap = new HashMap<>();
    public HashSet<Name> originalRules = new HashSet<>();
    public FirstSet.EmptyChecker emptyChecker = new FirstSet.EmptyChecker(this);

    public Tree() {
    }

    public static Tree makeTree(File path) {
        try {
            return AstVisitor.makeTree(path).prepare();
        } catch (Exception e) {
            e.addSuppressed(new RuntimeException(path.getAbsolutePath()));
            throw new RuntimeException(e);
        }
    }

    public List<TokenDecl> getTokens() {
        var res = new ArrayList<TokenDecl>();
        tokenBlocks.forEach(tb -> {
            res.addAll(tb.tokens);
            tb.modeBlocks.forEach(mb -> res.addAll(mb.tokens));
        });
        return res;
    }

    public Tree prepare() {
        PrepareTree.checkReferences(this);
        for (var decl : rules) {
            originalRules.add(decl.ref);
        }
        return this;
    }

    public boolean isOriginal(Name name) {
        return originalRules.contains(name);
    }

    public void addModeBlock(ModeBlock modeBlock, TokenBlock tokenBlock) {
        if (tokenBlocks.stream().
                anyMatch(tb -> tb.modeBlocks.stream().
                        anyMatch(mb -> mb.name.equals(modeBlock.name)))) {
            throw new RuntimeException("a block with same name exists");
        }
        tokenBlock.modeBlocks.add(modeBlock);
    }

    //merge two grammar files(lexer,parser)
    void mergeWith(Tree other) {
        for (var tb : other.tokenBlocks) {
            var res = new TokenBlock();
            tokenBlocks.add(res);
            for (var decl : tb.tokens) {
                addToken(decl, res);
            }
            for (var mb : tb.modeBlocks) {
                var newMb = new ModeBlock(mb.name);
                addModeBlock(newMb, res);
                for (var decl : mb.tokens) {
                    addToken(decl, newMb);
                }
            }
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

    public void addToken(TokenDecl token, TokenBlock block) {
        for (var tb : tokenBlocks) {
            //globals
            for (var decl : tb.tokens) {
                if (decl.name.equals(token.name)) {
                    throw new RuntimeException("token " + token + " already exists in block");
                }
            }
        }
        block.tokens.add(token);
    }

    public void addToken(TokenDecl token, ModeBlock modeBlock) {
        //same scope
        for (var decl : modeBlock.tokens) {
            if (decl.name.equals(token.name)) {
                throw new RuntimeException(String.format("token %s already exists in mode '%s'", token, modeBlock.name));
            }
        }
        modeBlock.tokens.add(token);
    }

    public void addRule(RuleDecl rule) {
        if (rules.stream().anyMatch(rd -> rd.ref.equals(rule.ref))) {
            throw new RuntimeException("duplicate rule: " + rule);
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
        return senderMap.getOrDefault(name, name);
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
        int pos = rules.indexOf(prev);
        rules.add(pos + 1, rule);
        rule.index = rules.size();
    }

    boolean isStr(Node node, String str) {
        if (node.isSequence()) {
            node = node.asSequence().normal();
        }
        return node.isString() && node.asString().value.equals(str);
    }

    //find token by string literal
    public TokenDecl getTokenByValue(String val) {
        //only globals
        for (var tb : tokenBlocks) {
            for (var decl : tb.tokens) {
                if (isStr(decl.rhs, val)) return decl;
                else if (decl.rhs.isOr()) {
                    for (var ch : decl.rhs.asOr()) {
                        if (isStr(ch, val)) {
                            return decl;
                        }
                    }
                }
            }
        }
        return null;
    }

    public TokenDecl getToken(String name) {
        for (var tb : tokenBlocks) {
            var decl = tb.getToken(name);
            if (decl != null) {
                return decl;
            }
        }
        return null;
    }

    public List<TokenDecl> getTokens(String name) {
        var list = new ArrayList<TokenDecl>();
        for (var tb : tokenBlocks) {
            var decl = tb.getToken(name);
            if (decl != null) {
                list.add(decl);
            }
        }
        return list;
    }

    //construct NFA from this grammar file
    public NFA makeNFA() {
        return NFABuilder.build(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lexerMembers != null) {
            sb.append("lexerMembers{\n");
            for (var member : lexerMembers.members) {
                sb.append(member).append("\n");
            }
            sb.append("}\n");
        }
        for (var tb : tokenBlocks) {
            sb.append("token{\n");
            for (var decl : tb.tokens) {
                sb.append("  ").append(decl).append("\n");
            }
            for (var mb : tb.modeBlocks) {
                sb.append("  ").append(mb.name).append("{\n");
                for (var decl : mb.tokens) {
                    sb.append("    ").append(decl).append("\n");
                }
                sb.append("  }\n");
            }
            sb.append("}\n");
        }

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
        return rules.stream().filter(rd -> rd.ref.equals(name)).findFirst().orElse(null);
    }

    public List<RuleDecl> getRules(Name name) {
        return rules.stream().filter(rd -> rd.ref.equals(name)).collect(Collectors.toList());
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
