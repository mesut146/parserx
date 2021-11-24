package mesut.parserx.dfa.parser;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import mesut.parserx.dfa.parser.Ast;

public class Parser{
    Lexer lexer;
    Token la;

    public Parser(Lexer lexer) throws IOException{
        this.lexer = lexer;
        la = lexer.next();
    }

    Token consume(int type){
        if(la.type != type){
            throw new RuntimeException("unexpected token: " + la + " expecting: " + type);
        }
        try{
            Token res = la;
            la = lexer.next();
            return res;
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    public Ast.nfa nfa(){
        Ast.nfa res = new Ast.nfa();
        res.startDecl = startDecl();
        res.nls = consume(Tokens.nls);
        res.finalDecl = finalDecl();
        res.nls2 = consume(Tokens.nls);
        do{
            res.trLine.add(trLine());
        }while(la.type == Tokens.NUM);
        return res;
    }

    public Ast.trLine trLine(){
        Ast.trLine res = new Ast.trLine();
        res.g1 = trLineg1();
        if(la.type == Tokens.nls){
            res.nls = consume(Tokens.nls);
        }
        return res;
    }

    public Ast.trLineg1 trLineg1(){
        Ast.trLineg1 res = new Ast.trLineg1();
        Token NUMf1 = consume(Tokens.NUM);
        switch(la.type){
            case Tokens.ARROW:
            {
                res.which = 1;
                res.trArrow = trArrow1(NUMf1);
                break;
            }
            case Tokens.NUM:
            {
                res.which = 2;
                res.trSimple = trSimple1(NUMf1);
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [ARROW,NUM] got: "+la);
            }
        }
        return res;
    }

    public Ast.startDecl startDecl(){
        Ast.startDecl res = new Ast.startDecl();
        res.START = consume(Tokens.START);
        res.EQ = consume(Tokens.EQ);
        res.NUM = consume(Tokens.NUM);
        return res;
    }

    public Ast.finalDecl finalDecl(){
        Ast.finalDecl res = new Ast.finalDecl();
        res.FINAL = consume(Tokens.FINAL);
        res.EQ = consume(Tokens.EQ);
        res.finalList = finalList();
        return res;
    }

    public Ast.finalList finalList(){
        Ast.finalList res = new Ast.finalList();
        Ast.namedState namedStatef1 = namedState();
        switch(la.type){
            case Tokens.NUM:
            {
                res.namedState.add(namedStatef1);
                res.which = 1;
                while(la.type == Tokens.NUM){
                    res.namedState.add(namedState());
                }
                break;
            }
            case Tokens.COMMA:
            {
                res.which = 2;
                Ast.finalList.Finallist2 finallist2 = res.finallist2 = new Ast.finalList.Finallist2();
                finallist2.namedState = namedStatef1;
                while(la.type == Tokens.COMMA){
                    finallist2.g1.add(finalListg1());
                }
                break;
            }
            default:{
                res.which = 2;
                Ast.finalList.Finallist2 finallist2 = res.finallist2 = new Ast.finalList.Finallist2();
                finallist2.namedState = namedStatef1;
                while(la.type == Tokens.COMMA){
                    finallist2.g1.add(finalListg1());
                }
            }
        }
        return res;
    }

    public Ast.finalListg1 finalListg1(){
        Ast.finalListg1 res = new Ast.finalListg1();
        res.COMMA = consume(Tokens.COMMA);
        res.namedState = namedState();
        return res;
    }

    public Ast.namedState namedState(){
        Ast.namedState res = new Ast.namedState();
        res.NUM = consume(Tokens.NUM);
        if(la.type == Tokens.LP){
            res.g1 = namedStateg1();
        }
        return res;
    }

    public Ast.namedStateg1 namedStateg1(){
        Ast.namedStateg1 res = new Ast.namedStateg1();
        res.LP = consume(Tokens.LP);
        res.IDENT = consume(Tokens.IDENT);
        res.RP = consume(Tokens.RP);
        return res;
    }

    public Ast.trArrow trArrow(){
        Ast.trArrow res = new Ast.trArrow();
        res.NUM = consume(Tokens.NUM);
        res.ARROW = consume(Tokens.ARROW);
        res.NUM2 = consume(Tokens.NUM);
        if(la.type == Tokens.COMMA){
            res.g1 = trArrowg1();
        }
        return res;
    }

    public Ast.trArrow trArrow1(Token NUMf1){
        Ast.trArrow res = new Ast.trArrow();
        res.NUM = NUMf1;
        res.ARROW = consume(Tokens.ARROW);
        res.NUM2 = consume(Tokens.NUM);
        if(la.type == Tokens.COMMA){
            res.g1 = trArrowg1();
        }
        return res;
    }

    public Ast.trArrowg1 trArrowg1(){
        Ast.trArrowg1 res = new Ast.trArrowg1();
        res.COMMA = consume(Tokens.COMMA);
        res.INPUT = INPUT();
        return res;
    }

    public Ast.trSimple trSimple(){
        Ast.trSimple res = new Ast.trSimple();
        res.NUM = consume(Tokens.NUM);
        res.NUM2 = consume(Tokens.NUM);
        if(la.type == Tokens.IDENT || la.type == Tokens.BRACKET || la.type == Tokens.ANY){
            res.INPUT = INPUT();
        }
        return res;
    }

    public Ast.trSimple trSimple1(Token NUMf1){
        Ast.trSimple res = new Ast.trSimple();
        res.NUM = NUMf1;
        res.NUM2 = consume(Tokens.NUM);
        if(la.type == Tokens.IDENT || la.type == Tokens.BRACKET || la.type == Tokens.ANY){
            res.INPUT = INPUT();
        }
        return res;
    }

    public Ast.INPUT INPUT(){
        Ast.INPUT res = new Ast.INPUT();
        switch(la.type){
            case Tokens.BRACKET:
            {
                res.which = 1;
                res.BRACKET = consume(Tokens.BRACKET);
                break;
            }
            case Tokens.IDENT:
            {
                res.which = 2;
                res.IDENT = consume(Tokens.IDENT);
                break;
            }
            case Tokens.ANY:
            {
                res.which = 3;
                res.ANY = consume(Tokens.ANY);
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [IDENT,BRACKET,ANY] got: "+la);
            }
        }
        return res;
    }

}
