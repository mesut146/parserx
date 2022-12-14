package mesut.parserx.regex.parser;

import java.io.IOException;

public class Parser {
    Lexer lexer;
    Token la;

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        la = lexer.next();
    }

    Token consume(int type) {
        if (la.type != type) {
            throw new RuntimeException("unexpected token: " + la + " expecting: " + type);
        }
        try {
            Token res = la;
            la = lexer.next();
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Ast.rhs rhs() {
        Ast.rhs res = new Ast.rhs();
        res.seq = seq();
        while (la.type == Tokens.BAR) {
            res.g1.add(rhsg1());
        }
        return res;
    }

    public Ast.rhsg1 rhsg1() {
        Ast.rhsg1 res = new Ast.rhsg1();
        res.BAR = consume(Tokens.BAR);
        res.seq = seq();
        return res;
    }

    public Ast.seq seq() {
        Ast.seq res = new Ast.seq();
        do {
            res.regex.add(regex());
        } while (la.type == Tokens.LPAREN || la.type == Tokens.DOT || la.type == Tokens.ESCAPED || la.type == Tokens.BOPEN || la.type == Tokens.CHAR || la.type == Tokens.MINUS);
        return res;
    }

    public Ast.regex regex() {
        Ast.regex res = new Ast.regex();
        res.simple = simple();
        if (la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS) {
            res.g1 = regexg1();
        }
        return res;
    }

    public Ast.regexg1 regexg1() {
        Ast.regexg1 res = new Ast.regexg1();
        switch (la.type) {
            case Tokens.QUES: {
                res.which = 1;
                res.QUES = consume(Tokens.QUES);
                break;
            }
            case Tokens.STAR: {
                res.which = 2;
                res.STAR = consume(Tokens.STAR);
                break;
            }
            case Tokens.PLUS: {
                res.which = 3;
                res.PLUS = consume(Tokens.PLUS);
                break;
            }
            default: {
                throw new RuntimeException("expecting one of [QUES,STAR,PLUS] got: " + la);
            }
        }
        return res;
    }

    public Ast.simple simple() {
        Ast.simple res = new Ast.simple();
        switch (la.type) {
            case Tokens.DOT:
            case Tokens.ESCAPED:
            case Tokens.CHAR:
            case Tokens.MINUS: {
                res.which = 1;
                res.normalChar = normalChar();
                break;
            }
            case Tokens.BOPEN: {
                res.which = 2;
                res.bracket = bracket();
                break;
            }
            case Tokens.LPAREN: {
                res.which = 3;
                Ast.simple.Simple3 simple3 = res.simple3 = new Ast.simple.Simple3();
                simple3.LPAREN = consume(Tokens.LPAREN);
                simple3.rhs = rhs();
                simple3.RPAREN = consume(Tokens.RPAREN);
                break;
            }
            default: {
                throw new RuntimeException("expecting one of [LPAREN,DOT,ESCAPED,BOPEN,CHAR,MINUS] got: " + la);
            }
        }
        return res;
    }

    public Ast.bracket bracket() {
        Ast.bracket res = new Ast.bracket();
        res.BOPEN = consume(Tokens.BOPEN);
        if (la.type == Tokens.XOR) {
            res.XOR = consume(Tokens.XOR);
        }
        boolean flag = true;
        boolean first = true;
        while (flag) {
            switch (la.type) {
                case Tokens.BAR:
                case Tokens.QUES:
                case Tokens.RPAREN:
                case Tokens.LPAREN:
                case Tokens.STAR:
                case Tokens.DOT:
                case Tokens.ESCAPED:
                case Tokens.BOPEN:
                case Tokens.CHAR:
                case Tokens.XOR:
                case Tokens.PLUS:
                case Tokens.MINUS: {
                    res.range.add(range());
                }
                break;
                default: {
                    if (!first) flag = false;
                    else throw new RuntimeException("unexpected token: " + la);
                }
            }
            first = false;

        }
        res.BCLOSE = consume(Tokens.BCLOSE);
        return res;
    }

    public Ast.range range() {
        Ast.range res = new Ast.range();
        res.rangeChar = rangeChar();
        if (la.type == Tokens.MINUS) {
            res.g1 = rangeg1();
        }
        return res;
    }

    public Ast.rangeg1 rangeg1() {
        Ast.rangeg1 res = new Ast.rangeg1();
        res.MINUS = consume(Tokens.MINUS);
        res.rangeChar = rangeChar();
        return res;
    }

    public Ast.normalChar normalChar() {
        Ast.normalChar res = new Ast.normalChar();
        switch (la.type) {
            case Tokens.CHAR: {
                res.which = 1;
                res.CHAR = consume(Tokens.CHAR);
                break;
            }
            case Tokens.ESCAPED: {
                res.which = 2;
                res.ESCAPED = consume(Tokens.ESCAPED);
                break;
            }
            case Tokens.MINUS: {
                res.which = 3;
                res.MINUS = consume(Tokens.MINUS);
                break;
            }
            case Tokens.DOT: {
                res.which = 4;
                res.DOT = consume(Tokens.DOT);
                break;
            }
            default: {
                throw new RuntimeException("expecting one of [DOT,ESCAPED,CHAR,MINUS] got: " + la);
            }
        }
        return res;
    }

    public Ast.rangeChar rangeChar() {
        Ast.rangeChar res = new Ast.rangeChar();
        switch (la.type) {
            case Tokens.CHAR: {
                res.which = 1;
                res.CHAR = consume(Tokens.CHAR);
                break;
            }
            case Tokens.ESCAPED: {
                res.which = 2;
                res.ESCAPED = consume(Tokens.ESCAPED);
                break;
            }
            case Tokens.STAR: {
                res.which = 3;
                res.STAR = consume(Tokens.STAR);
                break;
            }
            case Tokens.PLUS: {
                res.which = 4;
                res.PLUS = consume(Tokens.PLUS);
                break;
            }
            case Tokens.QUES: {
                res.which = 5;
                res.QUES = consume(Tokens.QUES);
                break;
            }
            case Tokens.BAR: {
                res.which = 6;
                res.BAR = consume(Tokens.BAR);
                break;
            }
            case Tokens.DOT: {
                res.which = 7;
                res.DOT = consume(Tokens.DOT);
                break;
            }
            case Tokens.LPAREN: {
                res.which = 8;
                res.LPAREN = consume(Tokens.LPAREN);
                break;
            }
            case Tokens.RPAREN: {
                res.which = 9;
                res.RPAREN = consume(Tokens.RPAREN);
                break;
            }
            case Tokens.XOR: {
                res.which = 10;
                res.XOR = consume(Tokens.XOR);
                break;
            }
            case Tokens.MINUS: {
                res.which = 11;
                res.MINUS = consume(Tokens.MINUS);
                break;
            }
            case Tokens.BOPEN: {
                res.which = 12;
                res.BOPEN = consume(Tokens.BOPEN);
                break;
            }
            default: {
                throw new RuntimeException("expecting one of [BAR,QUES,RPAREN,LPAREN,STAR,DOT,ESCAPED,BOPEN,CHAR,XOR,PLUS,MINUS] got: " + la);
            }
        }
        return res;
    }

}
