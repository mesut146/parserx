package mesut.parserx.parser;

import java.io.IOException;

public class Parser {
    TokenStream ts;

    public Parser(Lexer lexer) throws IOException {
        this.ts = new TokenStream(lexer);
    }

    public Ast.tokenBlockg1 tokenBlockg1() throws IOException {
        Ast.tokenBlockg1 res = new Ast.tokenBlockg1();
        switch (tokenBlockg1_decide()) {
            case 1: {
                Ast.tokenBlockg1.Alt1 tokenDecl = new Ast.tokenBlockg1.Alt1();
                tokenDecl.holder = res;
                res.tokenDecl = tokenDecl;
                res.which = 1;
                tokenDecl.tokenDecl = tokenDecl();
                break;
            }
            case 2: {
                Ast.tokenBlockg1.Alt2 modeBlock = new Ast.tokenBlockg1.Alt2();
                modeBlock.holder = res;
                res.modeBlock = modeBlock;
                res.which = 2;
                modeBlock.modeBlock = modeBlock();
                break;
            }
        }
        return res;
    }

    public int tokenBlockg1_decide() throws IOException {
        int which = 0;
        if (ts.la.type == Tokens.IDENT) {
            ts.pop();
            if (ts.la.type == Tokens.LBRACE) {
                ts.pop();
                which = 2;
            }
            else if (ts.la.type == Tokens.SEPARATOR) {
                ts.pop();
                which = 1;
            }
            else throw new RuntimeException("unexpected token: " + ts.la);
        }
        else if (ts.la.type == Tokens.HASH) {
            ts.pop();
            which = 1;
        }
        else throw new RuntimeException("unexpected token: " + ts.la);
        ts.unmark();
        return which;
    }

    public Ast.regex regex() throws IOException {
        Ast.regex res = new Ast.regex();
        switch (regex_decide()) {
            case 1: {
                Ast.regex.Alt1 alt1 = new Ast.regex.Alt1();
                alt1.holder = res;
                res.alt1 = alt1;
                res.which = 1;
                alt1.name = name();
                alt1.EQ = ts.consume(Tokens.EQ, "EQ");
                alt1.simple = simple();
                if (ts.la.type == Tokens.PLUS || ts.la.type == Tokens.QUES || ts.la.type == Tokens.STAR) {
                    alt1.type = regexType();
                }
                if (ts.la.type == Tokens.ACTION) {
                    alt1.ACTION = ts.consume(Tokens.ACTION, "ACTION");
                }
                break;
            }
            case 2: {
                Ast.regex.Alt2 alt2 = new Ast.regex.Alt2();
                alt2.holder = res;
                res.alt2 = alt2;
                res.which = 2;
                alt2.simple = simple();
                if (ts.la.type == Tokens.PLUS || ts.la.type == Tokens.QUES || ts.la.type == Tokens.STAR) {
                    alt2.type = regexType();
                }
                if (ts.la.type == Tokens.ACTION) {
                    alt2.ACTION = ts.consume(Tokens.ACTION, "ACTION");
                }
                break;
            }
        }
        return res;
    }

    public int regex_decide() throws IOException {
        int which = 0;
        if (ts.la.type == Tokens.IDENT) {
            ts.pop();
            which = 2;
            if (ts.la.type == Tokens.ACTION || ts.la.type == Tokens.EQ || ts.la.type == Tokens.PLUS || ts.la.type == Tokens.QUES || ts.la.type == Tokens.STAR) {
                if (ts.la.type == Tokens.ACTION) {
                    ts.pop();
                    which = 2;
                }
                else if (ts.la.type == Tokens.QUES) {
                    ts.pop();
                    which = 2;
                }
                else if (ts.la.type == Tokens.EQ) {
                    ts.pop();
                    which = 1;
                }
                else if (ts.la.type == Tokens.STAR) {
                    ts.pop();
                    which = 2;
                }
                else if (ts.la.type == Tokens.PLUS) {
                    ts.pop();
                    which = 2;
                }
                else throw new RuntimeException("unexpected token: " + ts.la);
            }
        }
        else if (ts.la.type == Tokens.CALL_BEGIN) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.BRACKET) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.TILDE) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.DOT) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.SHORTCUT) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.EPSILON) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.CHAR) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.LP) {
            ts.pop();
            which = 2;
        }
        else if (ts.la.type == Tokens.STRING) {
            ts.pop();
            which = 2;
        }
        else throw new RuntimeException("unexpected token: " + ts.la);
        ts.unmark();
        return which;
    }

    public Ast.tree tree() throws IOException {
        Ast.tree res = new Ast.tree();
        while (ts.la.type == Tokens.INCLUDE) {
            res.includeStatement.add(includeStatement());
        }
        if (ts.la.type == Tokens.OPTIONS) {
            res.optionsBlock = optionsBlock();
        }
        if (ts.la.type == Tokens.LEXER_MEMBERS_BEGIN) {
            res.lexerMembers = lexerMembers();
        }
        while (ts.la.type == Tokens.TOKEN) {
            res.tokens.add(tokenBlock());
        }
        if (ts.la.type == Tokens.START) {
            res.startDecl = startDecl();
        }
        while (ts.la.type == Tokens.IDENT) {
            res.rules.add(ruleDecl());
        }
        return res;
    }

    public Ast.lexerMembers lexerMembers() throws IOException {
        Ast.lexerMembers res = new Ast.lexerMembers();
        res.LEXER_MEMBERS_BEGIN = ts.consume(Tokens.LEXER_MEMBERS_BEGIN, "LEXER_MEMBERS_BEGIN");
        res.LEXER_MEMBER.add(ts.consume(Tokens.LEXER_MEMBER, "LEXER_MEMBER"));
        while (ts.la.type == Tokens.LEXER_MEMBER) {
            res.LEXER_MEMBER.add(ts.consume(Tokens.LEXER_MEMBER, "LEXER_MEMBER"));
        }
        res.MEMBERS_END = ts.consume(Tokens.MEMBERS_END, "MEMBERS_END");
        return res;
    }

    public Ast.includeStatement includeStatement() throws IOException {
        Ast.includeStatement res = new Ast.includeStatement();
        res.INCLUDE = ts.consume(Tokens.INCLUDE, "INCLUDE");
        res.STRING = ts.consume(Tokens.STRING, "STRING");
        return res;
    }

    public Ast.optionsBlock optionsBlock() throws IOException {
        Ast.optionsBlock res = new Ast.optionsBlock();
        res.OPTIONS = ts.consume(Tokens.OPTIONS, "OPTIONS");
        res.LBRACE = ts.consume(Tokens.LBRACE, "LBRACE");
        while (ts.la.type == Tokens.IDENT) {
            res.option.add(option());
        }
        res.RBRACE = ts.consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.option option() throws IOException {
        Ast.option res = new Ast.option();
        res.key = ts.consume(Tokens.IDENT, "IDENT");
        res.EQ = ts.consume(Tokens.EQ, "EQ");
        res.value = optiong1();
        if (ts.la.type == Tokens.SEMI) {
            res.SEMI = ts.consume(Tokens.SEMI, "SEMI");
        }
        return res;
    }

    public Ast.optiong1 optiong1() throws IOException {
        Ast.optiong1 res = new Ast.optiong1();
        if (ts.la.type == Tokens.NUMBER) {
            Ast.optiong1.Alt1 NUMBER = new Ast.optiong1.Alt1();
            NUMBER.holder = res;
            res.NUMBER = NUMBER;
            res.which = 1;
            NUMBER.NUMBER = ts.consume(Tokens.NUMBER, "NUMBER");
        }
        else if (ts.la.type == Tokens.BOOLEAN) {
            Ast.optiong1.Alt2 BOOLEAN = new Ast.optiong1.Alt2();
            BOOLEAN.holder = res;
            res.BOOLEAN = BOOLEAN;
            res.which = 2;
            BOOLEAN.BOOLEAN = ts.consume(Tokens.BOOLEAN, "BOOLEAN");
        }
        else throw new RuntimeException("expecting one of [BOOLEAN, NUMBER] got: " + ts.la);
        return res;
    }

    public Ast.startDecl startDecl() throws IOException {
        Ast.startDecl res = new Ast.startDecl();
        res.START = ts.consume(Tokens.START, "START");
        res.SEPARATOR = ts.consume(Tokens.SEPARATOR, "SEPARATOR");
        res.name = name();
        res.SEMI = ts.consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.tokenBlock tokenBlock() throws IOException {
        Ast.tokenBlock res = new Ast.tokenBlock();
        res.TOKEN = ts.consume(Tokens.TOKEN, "TOKEN");
        res.LBRACE = ts.consume(Tokens.LBRACE, "LBRACE");
        while (ts.la.type == Tokens.HASH || ts.la.type == Tokens.IDENT) {
            res.g1.add(tokenBlockg1());
        }
        res.RBRACE = ts.consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.tokenDecl tokenDecl() throws IOException {
        Ast.tokenDecl res = new Ast.tokenDecl();
        if (ts.la.type == Tokens.HASH) {
            res.HASH = ts.consume(Tokens.HASH, "HASH");
        }
        res.name = name();
        res.SEPARATOR = ts.consume(Tokens.SEPARATOR, "SEPARATOR");
        res.rhs = rhs();
        if (ts.la.type == Tokens.ARROW) {
            res.mode = tokenDeclg1();
        }
        res.SEMI = ts.consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.tokenDeclg1 tokenDeclg1() throws IOException {
        Ast.tokenDeclg1 res = new Ast.tokenDeclg1();
        res.ARROW = ts.consume(Tokens.ARROW, "ARROW");
        res.modes = modes();
        return res;
    }

    public Ast.modes modes() throws IOException {
        Ast.modes res = new Ast.modes();
        res.name = name();
        if (ts.la.type == Tokens.COMMA) {
            res.g1 = modesg1();
        }
        return res;
    }

    public Ast.modesg1 modesg1() throws IOException {
        Ast.modesg1 res = new Ast.modesg1();
        res.COMMA = ts.consume(Tokens.COMMA, "COMMA");
        res.name = name();
        return res;
    }

    public Ast.modeBlock modeBlock() throws IOException {
        Ast.modeBlock res = new Ast.modeBlock();
        res.IDENT = ts.consume(Tokens.IDENT, "IDENT");
        res.LBRACE = ts.consume(Tokens.LBRACE, "LBRACE");
        while (ts.la.type == Tokens.HASH || ts.la.type == Tokens.IDENT) {
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = ts.consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.ruleDecl ruleDecl() throws IOException {
        Ast.ruleDecl res = new Ast.ruleDecl();
        res.name = name();
        if (ts.la.type == Tokens.LP) {
            res.args = args();
        }
        res.SEPARATOR = ts.consume(Tokens.SEPARATOR, "SEPARATOR");
        res.rhs = rhs();
        res.SEMI = ts.consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.args args() throws IOException {
        Ast.args res = new Ast.args();
        res.LP = ts.consume(Tokens.LP, "LP");
        res.name = name();
        while (ts.la.type == Tokens.COMMA) {
            res.rest.add(argsg1());
        }
        res.RP = ts.consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.argsg1 argsg1() throws IOException {
        Ast.argsg1 res = new Ast.argsg1();
        res.COMMA = ts.consume(Tokens.COMMA, "COMMA");
        res.name = name();
        return res;
    }

    public Ast.rhs rhs() throws IOException {
        Ast.rhs res = new Ast.rhs();
        res.sequence = sequence();
        while (ts.la.type == Tokens.OR) {
            res.g1.add(rhsg1());
        }
        return res;
    }

    public Ast.rhsg1 rhsg1() throws IOException {
        Ast.rhsg1 res = new Ast.rhsg1();
        res.OR = ts.consume(Tokens.OR, "OR");
        res.sequence = sequence();
        return res;
    }

    public Ast.sequence sequence() throws IOException {
        Ast.sequence res = new Ast.sequence();
        res.sub.add(sub());
        while (ts.la.type == Tokens.BRACKET || ts.la.type == Tokens.CALL_BEGIN || ts.la.type == Tokens.CHAR || ts.la.type == Tokens.DOT || ts.la.type == Tokens.EPSILON || ts.la.type == Tokens.IDENT || ts.la.type == Tokens.LP || ts.la.type == Tokens.SHORTCUT || ts.la.type == Tokens.STRING || ts.la.type == Tokens.TILDE) {
            res.sub.add(sub());
        }
        if (ts.la.type == Tokens.LEFT || ts.la.type == Tokens.RIGHT) {
            res.assoc = sequenceg1();
        }
        if (ts.la.type == Tokens.HASH) {
            res.label = sequenceg2();
        }
        return res;
    }

    public Ast.sequenceg2 sequenceg2() throws IOException {
        Ast.sequenceg2 res = new Ast.sequenceg2();
        res.HASH = ts.consume(Tokens.HASH, "HASH");
        res.name = name();
        return res;
    }

    public Ast.sequenceg1 sequenceg1() throws IOException {
        Ast.sequenceg1 res = new Ast.sequenceg1();
        if (ts.la.type == Tokens.LEFT) {
            Ast.sequenceg1.Alt1 LEFT = new Ast.sequenceg1.Alt1();
            LEFT.holder = res;
            res.LEFT = LEFT;
            res.which = 1;
            LEFT.LEFT = ts.consume(Tokens.LEFT, "LEFT");
        }
        else if (ts.la.type == Tokens.RIGHT) {
            Ast.sequenceg1.Alt2 RIGHT = new Ast.sequenceg1.Alt2();
            RIGHT.holder = res;
            res.RIGHT = RIGHT;
            res.which = 2;
            RIGHT.RIGHT = ts.consume(Tokens.RIGHT, "RIGHT");
        }
        else throw new RuntimeException("expecting one of [LEFT, RIGHT] got: " + ts.la);
        return res;
    }

    public Ast.sub sub() throws IOException {
        Ast.sub res = new Ast.sub();
        res.regex = regex();
        if (ts.la.type == Tokens.MINUS) {
            res.g1 = subg1();
        }
        return res;
    }

    public Ast.subg1 subg1() throws IOException {
        Ast.subg1 res = new Ast.subg1();
        res.MINUS = ts.consume(Tokens.MINUS, "MINUS");
        res.stringNode = stringNode();
        return res;
    }

    public Ast.regexType regexType() throws IOException {
        Ast.regexType res = new Ast.regexType();
        if (ts.la.type == Tokens.STAR) {
            Ast.regexType.Alt1 STAR = new Ast.regexType.Alt1();
            STAR.holder = res;
            res.STAR = STAR;
            res.which = 1;
            STAR.STAR = ts.consume(Tokens.STAR, "STAR");
        }
        else if (ts.la.type == Tokens.PLUS) {
            Ast.regexType.Alt2 PLUS = new Ast.regexType.Alt2();
            PLUS.holder = res;
            res.PLUS = PLUS;
            res.which = 2;
            PLUS.PLUS = ts.consume(Tokens.PLUS, "PLUS");
        }
        else if (ts.la.type == Tokens.QUES) {
            Ast.regexType.Alt3 QUES = new Ast.regexType.Alt3();
            QUES.holder = res;
            res.QUES = QUES;
            res.which = 3;
            QUES.QUES = ts.consume(Tokens.QUES, "QUES");
        }
        else throw new RuntimeException("expecting one of [PLUS, QUES, STAR] got: " + ts.la);
        return res;
    }

    public Ast.simple simple() throws IOException {
        Ast.simple res = new Ast.simple();
        if (ts.la.type == Tokens.LP) {
            Ast.simple.Alt1 group = new Ast.simple.Alt1();
            group.holder = res;
            res.group = group;
            res.which = 1;
            group.group = group();
        }
        else if (ts.la.type == Tokens.IDENT) {
            Ast.simple.Alt2 name = new Ast.simple.Alt2();
            name.holder = res;
            res.name = name;
            res.which = 2;
            name.name = name();
        }
        else if (ts.la.type == Tokens.CHAR || ts.la.type == Tokens.STRING) {
            Ast.simple.Alt3 stringNode = new Ast.simple.Alt3();
            stringNode.holder = res;
            res.stringNode = stringNode;
            res.which = 3;
            stringNode.stringNode = stringNode();
        }
        else if (ts.la.type == Tokens.BRACKET) {
            Ast.simple.Alt4 bracketNode = new Ast.simple.Alt4();
            bracketNode.holder = res;
            res.bracketNode = bracketNode;
            res.which = 4;
            bracketNode.bracketNode = bracketNode();
        }
        else if (ts.la.type == Tokens.TILDE) {
            Ast.simple.Alt5 untilNode = new Ast.simple.Alt5();
            untilNode.holder = res;
            res.untilNode = untilNode;
            res.which = 5;
            untilNode.untilNode = untilNode();
        }
        else if (ts.la.type == Tokens.DOT) {
            Ast.simple.Alt6 dotNode = new Ast.simple.Alt6();
            dotNode.holder = res;
            res.dotNode = dotNode;
            res.which = 6;
            dotNode.dotNode = dotNode();
        }
        else if (ts.la.type == Tokens.EPSILON) {
            Ast.simple.Alt7 EPSILON = new Ast.simple.Alt7();
            EPSILON.holder = res;
            res.EPSILON = EPSILON;
            res.which = 7;
            EPSILON.EPSILON = ts.consume(Tokens.EPSILON, "EPSILON");
        }
        else if (ts.la.type == Tokens.SHORTCUT) {
            Ast.simple.Alt8 SHORTCUT = new Ast.simple.Alt8();
            SHORTCUT.holder = res;
            res.SHORTCUT = SHORTCUT;
            res.which = 8;
            SHORTCUT.SHORTCUT = ts.consume(Tokens.SHORTCUT, "SHORTCUT");
        }
        else if (ts.la.type == Tokens.CALL_BEGIN) {
            Ast.simple.Alt9 call = new Ast.simple.Alt9();
            call.holder = res;
            res.call = call;
            res.which = 9;
            call.call = call();
        }
        else
            throw new RuntimeException("expecting one of [BRACKET, CALL_BEGIN, CHAR, DOT, EPSILON, IDENT, LP, SHORTCUT, STRING, TILDE] got: " + ts.la);
        return res;
    }

    public Ast.group group() throws IOException {
        Ast.group res = new Ast.group();
        res.LP = ts.consume(Tokens.LP, "LP");
        res.rhs = rhs();
        res.RP = ts.consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.stringNode stringNode() throws IOException {
        Ast.stringNode res = new Ast.stringNode();
        if (ts.la.type == Tokens.STRING) {
            Ast.stringNode.Alt1 STRING = new Ast.stringNode.Alt1();
            STRING.holder = res;
            res.STRING = STRING;
            res.which = 1;
            STRING.STRING = ts.consume(Tokens.STRING, "STRING");
        }
        else if (ts.la.type == Tokens.CHAR) {
            Ast.stringNode.Alt2 CHAR = new Ast.stringNode.Alt2();
            CHAR.holder = res;
            res.CHAR = CHAR;
            res.which = 2;
            CHAR.CHAR = ts.consume(Tokens.CHAR, "CHAR");
        }
        else throw new RuntimeException("expecting one of [CHAR, STRING] got: " + ts.la);
        return res;
    }

    public Ast.bracketNode bracketNode() throws IOException {
        Ast.bracketNode res = new Ast.bracketNode();
        res.BRACKET = ts.consume(Tokens.BRACKET, "BRACKET");
        return res;
    }

    public Ast.untilNode untilNode() throws IOException {
        Ast.untilNode res = new Ast.untilNode();
        res.TILDE = ts.consume(Tokens.TILDE, "TILDE");
        res.regex = regex();
        return res;
    }

    public Ast.dotNode dotNode() throws IOException {
        Ast.dotNode res = new Ast.dotNode();
        res.DOT = ts.consume(Tokens.DOT, "DOT");
        return res;
    }

    public Ast.name name() throws IOException {
        Ast.name res = new Ast.name();
        res.IDENT = ts.consume(Tokens.IDENT, "IDENT");
        return res;
    }

    public Ast.call call() throws IOException {
        Ast.call res = new Ast.call();
        res.CALL_BEGIN = ts.consume(Tokens.CALL_BEGIN, "CALL_BEGIN");
        res.IDENT = ts.consume(Tokens.IDENT, "IDENT");
        while (ts.la.type == Tokens.COMMA) {
            res.g1.add(callg1());
        }
        res.RP = ts.consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.callg1 callg1() throws IOException {
        Ast.callg1 res = new Ast.callg1();
        res.COMMA = ts.consume(Tokens.COMMA, "COMMA");
        res.IDENT = ts.consume(Tokens.IDENT, "IDENT");
        return res;
    }
}
