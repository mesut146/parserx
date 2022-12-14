package mesut.parserx.dfa.parser;

import java.io.IOException;

public class Parser {
    Lexer lexer;
    Token la;

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        la = lexer.next();
    }

    Token consume(int type, String name) {
        if (la.type != type) {
            throw new RuntimeException("unexpected token: " + la + " expecting: " + name);
        }
        try {
            Token res = la;
            la = lexer.next();
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Ast.trLineg1 trLineg1() throws IOException {
        if (la.type == Tokens.NUM) {
            Ast.trLineg1 v0 = new Ast.trLineg1();
            Ast.trLineg1.Trlineg11 v1 = new Ast.trLineg1.Trlineg11();
            v1.holder = v0;
            Ast.trLineg1.Trlineg12 v2 = new Ast.trLineg1.Trlineg12();
            v2.holder = v0;
            Ast.trArrow v3 = new Ast.trArrow();
            Ast.trSimple v4 = new Ast.trSimple();
            v1.trArrow = v3;
            v2.trSimple = v4;
            v3.NUM = la;
            v4.NUM = la;
            v3.NUM = la;
            v4.NUM = la;
            la = lexer.next();
            S1(v1, v2, v3, v4);
            return v0;
        }
        else throw new RuntimeException("expecting one of [NUM] got: " + la);
    }

    public void S1(Ast.trLineg1.Trlineg11 p0, Ast.trLineg1.Trlineg12 p1, Ast.trArrow p2, Ast.trSimple p3) throws IOException {
        if (la.type == Tokens.NUM) {
            p3.NUM2 = consume(Tokens.NUM, "NUM");
            if (la.type == Tokens.ANY || la.type == Tokens.BRACKET || la.type == Tokens.IDENT || la.type == Tokens.NUM) {
                p3.INPUT = INPUT();
            }
            if (la.type == Tokens.EOF || la.type == Tokens.NUM || la.type == Tokens.nls) {
                p1.holder.which = 2;
                p1.holder.trSimple = p1;
            }
        }
        else if (la.type == Tokens.ARROW) {
            p2.ARROW = consume(Tokens.ARROW, "ARROW");
            p2.NUM2 = consume(Tokens.NUM, "NUM");
            if (la.type == Tokens.COMMA) {
                p2.g1 = trArrowg1();
            }
            if (la.type == Tokens.EOF || la.type == Tokens.NUM || la.type == Tokens.nls) {
                p0.holder.which = 1;
                p0.holder.trArrow = p0;
            }
        }
    }

    public Ast.nfa nfa() throws IOException {
        Ast.nfa res = new Ast.nfa();
        if (la.type == Tokens.nls) {
            res.nls = consume(Tokens.nls, "nls");
        }
        res.startDecl = startDecl();
        res.nls2 = consume(Tokens.nls, "nls");
        res.finalDecl = finalDecl();
        res.nls3 = consume(Tokens.nls, "nls");
        res.trLine.add(trLine());
        while (la.type == Tokens.NUM) {
            res.trLine.add(trLine());
        }
        return res;
    }

    public Ast.trLine trLine() throws IOException {
        Ast.trLine res = new Ast.trLine();
        res.g1 = trLineg1();
        if (la.type == Tokens.nls) {
            res.nls = consume(Tokens.nls, "nls");
        }
        return res;
    }

    public Ast.startDecl startDecl() throws IOException {
        Ast.startDecl res = new Ast.startDecl();
        res.START = consume(Tokens.START, "START");
        res.EQ = consume(Tokens.EQ, "EQ");
        res.NUM = consume(Tokens.NUM, "NUM");
        return res;
    }

    public Ast.finalDecl finalDecl() throws IOException {
        Ast.finalDecl res = new Ast.finalDecl();
        res.FINAL = consume(Tokens.FINAL, "FINAL");
        res.EQ = consume(Tokens.EQ, "EQ");
        res.finalList = finalList();
        return res;
    }

    public Ast.finalList finalList() throws IOException {
        Ast.finalList res = new Ast.finalList();
        res.namedState = namedState();
        while (la.type == Tokens.COMMA) {
            res.g1.add(finalListg1());
        }
        return res;
    }

    public Ast.finalListg1 finalListg1() throws IOException {
        Ast.finalListg1 res = new Ast.finalListg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.namedState = namedState();
        return res;
    }

    public Ast.namedState namedState() throws IOException {
        Ast.namedState res = new Ast.namedState();
        res.NUM = consume(Tokens.NUM, "NUM");
        if (la.type == Tokens.LP) {
            res.g1 = namedStateg1();
        }
        return res;
    }

    public Ast.namedStateg1 namedStateg1() throws IOException {
        Ast.namedStateg1 res = new Ast.namedStateg1();
        res.LP = consume(Tokens.LP, "LP");
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.trArrow trArrow() throws IOException {
        Ast.trArrow res = new Ast.trArrow();
        res.NUM = consume(Tokens.NUM, "NUM");
        res.ARROW = consume(Tokens.ARROW, "ARROW");
        res.NUM2 = consume(Tokens.NUM, "NUM");
        if (la.type == Tokens.COMMA) {
            res.g1 = trArrowg1();
        }
        return res;
    }

    public Ast.trArrowg1 trArrowg1() throws IOException {
        Ast.trArrowg1 res = new Ast.trArrowg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.INPUT = INPUT();
        return res;
    }

    public Ast.trSimple trSimple() throws IOException {
        Ast.trSimple res = new Ast.trSimple();
        res.NUM = consume(Tokens.NUM, "NUM");
        res.NUM2 = consume(Tokens.NUM, "NUM");
        if (la.type == Tokens.ANY || la.type == Tokens.BRACKET || la.type == Tokens.IDENT || la.type == Tokens.NUM) {
            res.INPUT = INPUT();
        }
        return res;
    }

    public Ast.INPUT INPUT() throws IOException {
        Ast.INPUT res = new Ast.INPUT();
        if (la.type == Tokens.BRACKET) {
            Ast.INPUT.Input1 BRACKET = new Ast.INPUT.Input1();
            BRACKET.holder = res;
            res.BRACKET = BRACKET;
            res.which = 1;
            BRACKET.BRACKET = consume(Tokens.BRACKET, "BRACKET");
        }
        else if (la.type == Tokens.IDENT) {
            Ast.INPUT.Input2 IDENT = new Ast.INPUT.Input2();
            IDENT.holder = res;
            res.IDENT = IDENT;
            res.which = 2;
            IDENT.IDENT = consume(Tokens.IDENT, "IDENT");
        }
        else if (la.type == Tokens.ANY) {
            Ast.INPUT.Input3 ANY = new Ast.INPUT.Input3();
            ANY.holder = res;
            res.ANY = ANY;
            res.which = 3;
            ANY.ANY = consume(Tokens.ANY, "ANY");
        }
        else if (la.type == Tokens.NUM) {
            Ast.INPUT.Input4 NUM = new Ast.INPUT.Input4();
            NUM.holder = res;
            res.NUM = NUM;
            res.which = 4;
            NUM.NUM = consume(Tokens.NUM, "NUM");
        }
        else throw new RuntimeException("expecting one of [ANY, BRACKET, IDENT, NUM] got: " + la);
        return res;
    }
}
