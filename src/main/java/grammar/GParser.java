/* GParser.java */
/* Generated By:JavaCC: Do not edit this line. GParser.java */
package grammar;

import nodes.*;
import rule.RuleDecl;
import utils.UnicodeUtils;

import java.io.File;

public class GParser implements GParserConstants {

    static private final LookaheadSuccess jj_ls = new LookaheadSuccess();
    static private int[] jj_la1_0;
    static private int[] jj_la1_1;

    static {
        jj_la1_init_0();
        jj_la1_init_1();
    }

    final private int[] jj_la1 = new int[15];
    final private JJCalls[] jj_2_rtns = new JJCalls[3];
    /**
     * Generated Token Manager.
     */
    public GParserTokenManager token_source;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    JavaCharStream jj_input_stream;
    private int jj_ntk;
    private Token jj_scanpos, jj_lastpos;
    private int jj_la;
    private int jj_gen;
    private boolean jj_rescan = false;
    private int jj_gc = 0;
    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
    private int[] jj_expentry;
    private int jj_kind = -1;
    private int[] jj_lasttokens = new int[100];
    private int jj_endpos;
    private boolean trace_enabled;

    /**
     * Constructor with InputStream.
     */
    public GParser(java.io.InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    public GParser(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new JavaCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new GParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Constructor.
     */
    public GParser(java.io.Reader stream) {
        jj_input_stream = new JavaCharStream(stream, 1, 1);
        token_source = new GParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Constructor with generated Token Manager.
     */
    public GParser(GParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{0x0, 0x0, 0x0, 0x0, 0x80000000, 0x80000000, 0x3880000, 0x0, 0x40105000, 0x40105000, 0x40105000, 0x1c000000, 0x1c000000, 0x40105000, 0x4000,};
    }

    private static void jj_la1_init_1() {
        jj_la1_1 = new int[]{0x10, 0x5, 0x20, 0x40, 0x40, 0x0, 0x0, 0x20000, 0x8048, 0x8048, 0x8048, 0x0, 0x0, 0x8048, 0x40,};
    }

    //--------parser rules-------------------
    final public Tree tree(File file) throws ParseException {
        Tree tree = new Tree();
        tree.file = file;
        label_1:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case INCLUDE_DIRECTIVE: {
                    ;
                    break;
                }
                default:
                    jj_la1[0] = jj_gen;
                    break label_1;
            }
            includeStatement(tree);
        }
        label_2:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case KEYWORD_TOKEN:
                case KEYWORD_SKIP: {
                    ;
                    break;
                }
                default:
                    jj_la1[1] = jj_gen;
                    break label_2;
            }
            if (jj_2_1(2147483647)) {
                tokenBlock(tree);
            }
            else if (jj_2_2(2147483647)) {
                skipBlock(tree);
            }
            else {
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case START_SIRECTIVE: {
                startDecl(tree);
                break;
            }
            default:
                jj_la1[2] = jj_gen;
                ;
        }
        label_3:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case IDENT: {
                    ;
                    break;
                }
                default:
                    jj_la1[3] = jj_gen;
                    break label_3;
            }
            ruleDecl(tree);
        }
        {
            if ("" != null) return tree;
        }
        throw new Error("Missing return statement in function");
    }

    final public void includeStatement(Tree tree) throws ParseException {
        Token tok;
        jj_consume_token(INCLUDE_DIRECTIVE);
        tok = jj_consume_token(STRING_LITERAL);
        tree.addInclude(UnicodeUtils.trimQuotes(tok.image));
    }

    //lexer rules
    final public void tokenBlock(Tree tree) throws ParseException {
        jj_consume_token(KEYWORD_TOKEN);
        jj_consume_token(LBRACE);
        tokenList(tree, false);
        jj_consume_token(RBRACE);
    }

    final public void tokenList(Tree tree, boolean skip) throws ParseException {
        label_4:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case HASH:
                case IDENT: {
                    ;
                    break;
                }
                default:
                    jj_la1[4] = jj_gen;
                    break label_4;
            }
            tokenDecl(tree, skip);
        }
    }

    final public void tokenDecl(Tree tree, boolean skip) throws ParseException {
        String name;
        boolean frag = false;
        TokenDecl decl;
        Node rhs;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case HASH: {
                jj_consume_token(HASH);
                frag = true;
                break;
            }
            default:
                jj_la1[5] = jj_gen;
                ;
        }
        name = name();
        jj_consume_token(EQ);
        rhs = rhs();
        jj_consume_token(SEMI);
        decl = new TokenDecl(name);
        decl.fragment = frag;
        decl.regex = rhs;
        if (skip) {
            tree.addSkip(decl);
        }
        else {
            tree.addToken(decl);
        }
    }

    final public void skipBlock(Tree tree) throws ParseException {
        jj_consume_token(KEYWORD_SKIP);
        jj_consume_token(LBRACE);
        tokenList(tree, true);
        jj_consume_token(RBRACE);
    }

    final public void declSeparator() throws ParseException {
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case EQ: {
                jj_consume_token(EQ);
                break;
            }
            case COLON: {
                jj_consume_token(COLON);
                break;
            }
            case COLONEQEQ: {
                jj_consume_token(COLONEQEQ);
                break;
            }
            case COLONEQ: {
                jj_consume_token(COLONEQ);
                break;
            }
            default:
                jj_la1[6] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
    }

    final public void ruleDecl(Tree tree) throws ParseException {
        RuleDecl decl = new RuleDecl();
        String name;
        Node rhs;
        name = name();
        declSeparator();
        rhs = rhs();
        jj_consume_token(SEMI);
        decl.name = name;
        decl.rhs = rhs;
        tree.addRule(decl);
    }

    final public void startDecl(Tree tree) throws ParseException {
        jj_consume_token(START_SIRECTIVE);
        declSeparator();
        tree.start = ref();
        jj_consume_token(SEMI);
    }

    //or list
    final public Node rhs() throws ParseException {
        Node rule;
        OrNode or = new OrNode();
        boolean more = false;
        rule = orNode();
        or.add(rule);
        label_5:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case 49: {
                    ;
                    break;
                }
                default:
                    jj_la1[7] = jj_gen;
                    break label_5;
            }
            jj_consume_token(49);
            rule = orNode();
            or.add(rule);
            more = true;
        }
        {
            if ("" != null) return more ? or : rule;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node orNode() throws ParseException {
        Node node = new EmptyNode();
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case LPAREN:
            case LBRACE:
            case DOT:
            case TILDE:
            case STRING_LITERAL:
            case IDENT:
            case BRACKET_LIST: {
                node = sequence();
                break;
            }
            default:
                jj_la1[8] = jj_gen;
                ;
        }
        {
            if ("" != null) return node;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node sequence() throws ParseException {
        Sequence s = new Sequence();
        Node r;
        label_6:
        while (true) {
            r = regex();
            s.add(r);
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case LPAREN:
                case LBRACE:
                case DOT:
                case TILDE:
                case STRING_LITERAL:
                case IDENT:
                case BRACKET_LIST: {
                    ;
                    break;
                }
                default:
                    jj_la1[9] = jj_gen;
                    break label_6;
            }
        }
        {
            if ("" != null) return s.normal();
        }
        throw new Error("Missing return statement in function");
    }

    final public Node regex() throws ParseException {
        Node rule;
        String type;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case LPAREN:
            case LBRACE:
            case DOT:
            case TILDE:
            case STRING_LITERAL:
            case IDENT:
            case BRACKET_LIST: {
                rule = simple();
                break;
            }
            default:
                jj_la1[10] = jj_gen;
                if (jj_2_3(2147483647)) {
                    rule = repetition();
                }
                else {
                    jj_consume_token(-1);
                    throw new ParseException();
                }
        }
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case STAR:
            case PLUS:
            case QUES: {
                type = regexType();
                {
                    if ("" != null) return new RegexNode(rule, type);
                }
                break;
            }
            default:
                jj_la1[11] = jj_gen;
                ;
        }
        {
            if ("" != null) return rule;
        }
        throw new Error("Missing return statement in function");
    }

    final public String regexType() throws ParseException {
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case STAR: {
                jj_consume_token(STAR);
                break;
            }
            case PLUS: {
                jj_consume_token(PLUS);
                break;
            }
            case QUES: {
                jj_consume_token(QUES);
                break;
            }
            default:
                jj_la1[12] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if ("" != null) return getToken(0).image;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node simple() throws ParseException {
        Node rule;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case LPAREN: {
                rule = group();
                break;
            }
            case LBRACE:
            case IDENT: {
                rule = ref();
                break;
            }
            case STRING_LITERAL: {
                rule = stringNode();
                break;
            }
            case BRACKET_LIST: {
                rule = bracketNode();
                break;
            }
            case TILDE: {
                rule = untilNode();
                break;
            }
            case DOT: {
                rule = dotNode();
                break;
            }
            default:
                jj_la1[13] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if ("" != null) return rule;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node dotNode() throws ParseException {
        jj_consume_token(DOT);
        StringNode str = new StringNode();
        str.isDot = true;
        {
            if ("" != null) return str;
        }
        throw new Error("Missing return statement in function");
    }

    //[a-z]
    final public Node bracketNode() throws ParseException {
        Bracket b = new Bracket();
        Token t;
        t = jj_consume_token(BRACKET_LIST);
        b.parse(t.image);
        {
            if ("" != null) return b;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node group() throws ParseException {
        Node rule;
        jj_consume_token(LPAREN);
        rule = rhs();
        jj_consume_token(RPAREN);
        {
            if ("" != null) return new GroupNode(rule);
        }
        throw new Error("Missing return statement in function");
    }

    final public Node untilNode() throws ParseException {
        Node node;
        jj_consume_token(TILDE);
        node = regex();
        {
            if ("" != null) return new UntilNode(node);
        }
        throw new Error("Missing return statement in function");
    }

    final public Node stringNode() throws ParseException {
        Token tok;
        tok = jj_consume_token(STRING_LITERAL);
        {
            if ("" != null) return StringNode.from(tok.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public NameNode lexerRef() throws ParseException {
        String name;
        jj_consume_token(LBRACE);
        name = name();
        jj_consume_token(RBRACE);
        {
            if ("" != null) return new NameNode(name, true);
        }
        throw new Error("Missing return statement in function");
    }

    final public NameNode ref() throws ParseException {
        String name;
        NameNode ref;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case IDENT: {
                name = name();
                ref = new NameNode(name);
                break;
            }
            case LBRACE: {
                ref = lexerRef();
                break;
            }
            default:
                jj_la1[14] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if ("" != null) return ref;
        }
        throw new Error("Missing return statement in function");
    }

    final public String name() throws ParseException {
        Token token;
        token = jj_consume_token(IDENT);
        {
            if ("" != null) return token.image;
        }
        throw new Error("Missing return statement in function");
    }

    final public Node repetition() throws ParseException {
        Node node;
        Token num;
        node = simple();
        jj_consume_token(LBRACE);
        num = jj_consume_token(INTEGER);
        jj_consume_token(RBRACE);
        {
            if ("" != null) return new Repetition(node, Integer.valueOf(num.toString()));
        }
        throw new Error("Missing return statement in function");
    }

    private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_1());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_2());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private boolean jj_2_3(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_3());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(2, xla);
        }
    }

    private boolean jj_3R_12() {
        if (jj_3R_18()) return true;
        return false;
    }

    private boolean jj_3R_32() {
        if (jj_3R_34()) return true;
        return false;
    }

    private boolean jj_3R_11() {
        if (jj_3R_17()) return true;
        return false;
    }

    private boolean jj_3R_25() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_32()) jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_10() {
        if (jj_3R_16()) return true;
        return false;
    }

    private boolean jj_3R_19() {
        if (jj_scan_token(TILDE)) return true;
        if (jj_3R_24()) return true;
        return false;
    }

    private boolean jj_3R_27() {
        if (jj_scan_token(IDENT)) return true;
        return false;
    }

    private boolean jj_3R_9() {
        if (jj_3R_15()) return true;
        return false;
    }

    private boolean jj_3_1() {
        if (jj_scan_token(KEYWORD_TOKEN)) return true;
        if (jj_scan_token(LBRACE)) return true;
        return false;
    }

    private boolean jj_3R_8() {
        Token xsp;
        xsp = jj_scanpos;
        if (!jj_3R_9()) return false;
        jj_scanpos = xsp;
        if (!jj_3R_10()) return false;
        jj_scanpos = xsp;
        if (!jj_3R_11()) return false;
        jj_scanpos = xsp;
        if (!jj_3R_12()) return false;
        jj_scanpos = xsp;
        if (!jj_3R_13()) return false;
        jj_scanpos = xsp;
        if (jj_3R_14()) return true;
        return false;
    }

    private boolean jj_3R_30() {
        if (jj_3R_7()) return true;
        return false;
    }

    private boolean jj_3_2() {
        if (jj_scan_token(KEYWORD_SKIP)) return true;
        if (jj_scan_token(LBRACE)) return true;
        return false;
    }

    private boolean jj_3R_26() {
        if (jj_scan_token(49)) return true;
        if (jj_3R_25()) return true;
        return false;
    }

    private boolean jj_3R_23() {
        if (jj_3R_28()) return true;
        return false;
    }

    private boolean jj_3R_15() {
        if (jj_scan_token(LPAREN)) return true;
        if (jj_3R_21()) return true;
        if (jj_scan_token(RPAREN)) return true;
        return false;
    }

    private boolean jj_3R_21() {
        if (jj_3R_25()) return true;
        Token xsp;
        while (true) {
            xsp = jj_scanpos;
            if (jj_3R_26()) {
                jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private boolean jj_3R_33() {
        Token xsp;
        xsp = jj_scanpos;
        if (!jj_scan_token(26)) return false;
        jj_scanpos = xsp;
        if (!jj_scan_token(27)) return false;
        jj_scanpos = xsp;
        if (jj_scan_token(28)) return true;
        return false;
    }

    private boolean jj_3R_22() {
        if (jj_3R_27()) return true;
        return false;
    }

    private boolean jj_3R_16() {
        Token xsp;
        xsp = jj_scanpos;
        if (!jj_3R_22()) return false;
        jj_scanpos = xsp;
        if (jj_3R_23()) return true;
        return false;
    }

    private boolean jj_3R_31() {
        if (jj_3R_33()) return true;
        return false;
    }

    private boolean jj_3R_29() {
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3R_24() {
        Token xsp;
        xsp = jj_scanpos;
        if (!jj_3R_29()) return false;
        jj_scanpos = xsp;
        if (jj_3R_30()) return true;
        xsp = jj_scanpos;
        if (jj_3R_31()) jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_18() {
        if (jj_scan_token(BRACKET_LIST)) return true;
        return false;
    }

    private boolean jj_3R_28() {
        if (jj_scan_token(LBRACE)) return true;
        if (jj_3R_27()) return true;
        if (jj_scan_token(RBRACE)) return true;
        return false;
    }

    private boolean jj_3R_35() {
        if (jj_3R_24()) return true;
        return false;
    }

    private boolean jj_3R_34() {
        Token xsp;
        if (jj_3R_35()) return true;
        while (true) {
            xsp = jj_scanpos;
            if (jj_3R_35()) {
                jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private boolean jj_3R_20() {
        if (jj_scan_token(DOT)) return true;
        return false;
    }

    private boolean jj_3R_17() {
        if (jj_scan_token(STRING_LITERAL)) return true;
        return false;
    }

    private boolean jj_3R_7() {
        if (jj_3R_8()) return true;
        if (jj_scan_token(LBRACE)) return true;
        if (jj_scan_token(INTEGER)) return true;
        if (jj_scan_token(RBRACE)) return true;
        return false;
    }

    private boolean jj_3_3() {
        if (jj_3R_7()) return true;
        return false;
    }

    private boolean jj_3R_14() {
        if (jj_3R_20()) return true;
        return false;
    }

    private boolean jj_3R_13() {
        if (jj_3R_19()) return true;
        return false;
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream) {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.Reader stream) {
        if (jj_input_stream == null) {
            jj_input_stream = new JavaCharStream(stream, 1, 1);
        }
        else {
            jj_input_stream.ReInit(stream, 1, 1);
        }
        if (token_source == null) {
            token_source = new GParserTokenManager(jj_input_stream);
        }

        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Reinitialise.
     */
    public void ReInit(GParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 15; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) c.first = null;
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            }
            else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        }
        else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) jj_add_error_token(kind, i);
        }
        if (jj_scanpos.kind != kind) return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
        return false;
    }

    /**
     * Get the next Token.
     */
    final public Token getNextToken() {
        if (token.next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next;
            else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private int jj_ntk_f() {
        if ((jj_nt = token.next) == null)
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }

        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        }
        else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];

            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }

            for (int[] oldentry : jj_expentries) {
                if (oldentry.length == jj_expentry.length) {
                    boolean isMatched = true;

                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            isMatched = false;
                            break;
                        }

                    }
                    if (isMatched) {
                        jj_expentries.add(jj_expentry);
                        break;
                    }
                }
            }

            if (pos != 0) {
                jj_lasttokens[(jj_endpos = pos) - 1] = kind;
            }
        }
    }

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[50];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 15; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 50; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /**
     * Trace enabled.
     */
    final public boolean trace_enabled() {
        return trace_enabled;
    }

    /**
     * Enable tracing.
     */
    final public void enable_tracing() {
    }

    /**
     * Disable tracing.
     */
    final public void disable_tracing() {
    }

    private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 3; i++) {
            try {
                JJCalls p = jj_2_rtns[i];

                do {
                    if (p.gen > jj_gen) {
                        jj_la = p.arg;
                        jj_lastpos = jj_scanpos = p.first;
                        switch (i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);

            } catch (LookaheadSuccess ls) {
            }
        }
        jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }

        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    @SuppressWarnings("serial")
    static private final class LookaheadSuccess extends java.lang.Error {
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;
    }

}
