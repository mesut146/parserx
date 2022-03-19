package mesut.parserx.parser;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import mesut.parserx.parser.Ast;

public class Parser{
    Lexer lexer;
    Token la;

    public Parser(Lexer lexer) throws IOException{
        this.lexer = lexer;
        la = lexer.next();
    }

    Token consume(int type, String name){
        if(la.type != type){
            throw new RuntimeException("unexpected token: " + la + " expecting: " + name);
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
    public Ast.tree tree(){
        Ast.tree res = new Ast.tree();
        while(la.type == Tokens.INCLUDE){
            res.includeStatement.add(includeStatement());
        }
        if(la.type == Tokens.OPTIONS){
            res.optionsBlock = optionsBlock();
        }
        while(la.type == Tokens.TOKEN || la.type == Tokens.SKIP){
            res.tokens.add(treeg1());
        }
        if(la.type == Tokens.START){
            res.startDecl = startDecl();
        }
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.TOKEN || la.type == Tokens.SKIP || la.type == Tokens.INCLUDE){
            res.rules.add(ruleDecl());
        }
        return res;
    }

    public Ast.treeg1 treeg1(){
        Ast.treeg1 res = new Ast.treeg1();
        switch(la.type){
            case Tokens.TOKEN:
            {
                res.which = 1;
                res.tokenBlock = tokenBlock();
                break;
            }
            case Tokens.SKIP:
            {
                res.which = 2;
                res.skipBlock = skipBlock();
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [TOKEN,SKIP] got: "+la);
            }
        }
        return res;
    }

    public Ast.includeStatement includeStatement(){
        Ast.includeStatement res = new Ast.includeStatement();
        res.INCLUDE = consume(Tokens.INCLUDE, "INCLUDE");
        res.STRING = consume(Tokens.STRING, "STRING");
        return res;
    }

    public Ast.optionsBlock optionsBlock(){
        Ast.optionsBlock res = new Ast.optionsBlock();
        res.OPTIONS = consume(Tokens.OPTIONS, "OPTIONS");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.IDENT){
            res.option.add(option());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.option option(){
        Ast.option res = new Ast.option();
        res.key = consume(Tokens.IDENT, "IDENT");
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.value = optiong1();
        if(la.type == Tokens.SEMI){
            res.SEMI = consume(Tokens.SEMI, "SEMI");
        }
        return res;
    }

    public Ast.optiong1 optiong1(){
        Ast.optiong1 res = new Ast.optiong1();
        switch(la.type){
            case Tokens.NUMBER:
            {
                res.which = 1;
                res.NUMBER = consume(Tokens.NUMBER, "NUMBER");
                break;
            }
            case Tokens.BOOLEAN:
            {
                res.which = 2;
                res.BOOLEAN = consume(Tokens.BOOLEAN, "BOOLEAN");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [BOOLEAN,NUMBER] got: "+la);
            }
        }
        return res;
    }

    public Ast.startDecl startDecl(){
        Ast.startDecl res = new Ast.startDecl();
        res.START = consume(Tokens.START, "START");
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.name = name();
        res.SEMI = consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.tokenBlock tokenBlock(){
        Ast.tokenBlock res = new Ast.tokenBlock();
        res.TOKEN = consume(Tokens.TOKEN, "TOKEN");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.TOKEN || la.type == Tokens.SKIP || la.type == Tokens.HASH || la.type == Tokens.INCLUDE){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.skipBlock skipBlock(){
        Ast.skipBlock res = new Ast.skipBlock();
        res.SKIP = consume(Tokens.SKIP, "SKIP");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.TOKEN || la.type == Tokens.SKIP || la.type == Tokens.HASH || la.type == Tokens.INCLUDE){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.tokenDecl tokenDecl(){
        Ast.tokenDecl res = new Ast.tokenDecl();
        if(la.type == Tokens.HASH){
            res.HASH = consume(Tokens.HASH, "HASH");
        }
        res.name = name();
        if(la.type == Tokens.MINUS){
            res.g1 = tokenDeclg1();
        }
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.rhs = rhs();
        res.SEMI = consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.tokenDeclg1 tokenDeclg1(){
        Ast.tokenDeclg1 res = new Ast.tokenDeclg1();
        res.MINUS = consume(Tokens.MINUS, "MINUS");
        res.name = name();
        return res;
    }

    public Ast.ruleDecl ruleDecl(){
        Ast.ruleDecl res = new Ast.ruleDecl();
        res.name = name();
        if(la.type == Tokens.LP){
            res.args = args();
        }
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.rhs = rhs();
        res.SEMI = consume(Tokens.SEMI, "SEMI");
        return res;
    }

    public Ast.args args(){
        Ast.args res = new Ast.args();
        res.LP = consume(Tokens.LP, "LP");
        res.name = name();
        while(la.type == Tokens.COMMA){
            res.rest.add(argsg1());
        }
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.argsg1 argsg1(){
        Ast.argsg1 res = new Ast.argsg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.name = name();
        return res;
    }

    public Ast.rhs rhs(){
        Ast.rhs res = new Ast.rhs();
        res.sequence = sequence();
        while(la.type == Tokens.OR){
            res.g1.add(rhsg1());
        }
        return res;
    }

    public Ast.rhsg1 rhsg1(){
        Ast.rhsg1 res = new Ast.rhsg1();
        res.OR = consume(Tokens.OR, "OR");
        res.sequence = sequence();
        return res;
    }

    public Ast.sequence sequence(){
        Ast.sequence res = new Ast.sequence();
        boolean flag = true;
        boolean first = true;
        while(flag){
            switch(la.type){
                case Tokens.CALL_BEGIN:
                case Tokens.OPTIONS:
                case Tokens.BRACKET:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                case Tokens.TILDE:
                case Tokens.DOT:
                case Tokens.EPSILON:
                case Tokens.CHAR:
                case Tokens.LP:
                case Tokens.INCLUDE:
                case Tokens.IDENT:
                case Tokens.LBRACE:
                case Tokens.SHORTCUT:
                case Tokens.STRING:
                {
                    res.regex.add(regex());
                }
                break;
                default:{
                    if(!first)  flag = false;
                    else  throw new RuntimeException("unexpected token: "+la);
                }
            }
            first = false;

        }
        if(la.type == Tokens.LEFT || la.type == Tokens.RIGHT){
            res.assoc = sequenceg1();
        }
        if(la.type == Tokens.HASH){
            res.label = sequenceg2();
        }
        return res;
    }

    public Ast.sequenceg2 sequenceg2(){
        Ast.sequenceg2 res = new Ast.sequenceg2();
        res.HASH = consume(Tokens.HASH, "HASH");
        res.name = name();
        return res;
    }

    public Ast.sequenceg1 sequenceg1(){
        Ast.sequenceg1 res = new Ast.sequenceg1();
        switch(la.type){
            case Tokens.LEFT:
            {
                res.which = 1;
                res.LEFT = consume(Tokens.LEFT, "LEFT");
                break;
            }
            case Tokens.RIGHT:
            {
                res.which = 2;
                res.RIGHT = consume(Tokens.RIGHT, "RIGHT");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [LEFT,RIGHT] got: "+la);
            }
        }
        return res;
    }

    public Ast.regex regex(){
        Ast.regex res = new Ast.regex();
        switch(la.type){
            case Tokens.IDENT:
            case Tokens.OPTIONS:
            case Tokens.TOKEN:
            case Tokens.SKIP:
            case Tokens.INCLUDE:
            {
                Ast.name namef1 = name();
                switch(la.type){
                    case Tokens.SEPARATOR:
                    {
                        res.which = 1;
                        Ast.regex.Regex1 regex1 = res.regex1 = new Ast.regex.Regex1();
                        regex1.name = namef1;
                        regex1.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
                        regex1.simple = simple();
                        if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                            regex1.type = regexg1();
                        }
                        break;
                    }
                    case Tokens.QUES:
                    case Tokens.STAR:
                    case Tokens.PLUS:
                    {
                        res.which = 2;
                        Ast.regex.Regex2 regex2 = res.regex2 = new Ast.regex.Regex2();
                        regex2.simple = simple_name(namef1);
                        if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                            regex2.type = regexg2();
                        }
                        break;
                    }
                    default:{
                        res.which = 2;
                        Ast.regex.Regex2 regex2 = res.regex2 = new Ast.regex.Regex2();
                        regex2.simple = simple_name(namef1);
                        if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                            regex2.type = regexg2();
                        }
                    }
                }
                break;
            }
            case Tokens.CALL_BEGIN:
            case Tokens.BRACKET:
            case Tokens.LBRACE:
            case Tokens.TILDE:
            case Tokens.DOT:
            case Tokens.SHORTCUT:
            case Tokens.EPSILON:
            case Tokens.CHAR:
            case Tokens.LP:
            case Tokens.STRING:
            {
                res.which = 2;
                Ast.regex.Regex2 regex2 = res.regex2 = new Ast.regex.Regex2();
                regex2.simple = simple_no_name();
                if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                    regex2.type = regexg2();
                }
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [CALL_BEGIN,OPTIONS,BRACKET,TOKEN,SKIP,TILDE,DOT,EPSILON,CHAR,LP,INCLUDE,IDENT,LBRACE,SHORTCUT,STRING] got: "+la);
            }
        }
        return res;
    }

    public Ast.regexg2 regexg2(){
        Ast.regexg2 res = new Ast.regexg2();
        switch(la.type){
            case Tokens.STAR:
            {
                res.which = 1;
                res.STAR = consume(Tokens.STAR, "STAR");
                break;
            }
            case Tokens.PLUS:
            {
                res.which = 2;
                res.PLUS = consume(Tokens.PLUS, "PLUS");
                break;
            }
            case Tokens.QUES:
            {
                res.which = 3;
                res.QUES = consume(Tokens.QUES, "QUES");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [QUES,STAR,PLUS] got: "+la);
            }
        }
        return res;
    }

    public Ast.regexg1 regexg1(){
        Ast.regexg1 res = new Ast.regexg1();
        switch(la.type){
            case Tokens.STAR:
            {
                res.which = 1;
                res.STAR = consume(Tokens.STAR, "STAR");
                break;
            }
            case Tokens.PLUS:
            {
                res.which = 2;
                res.PLUS = consume(Tokens.PLUS, "PLUS");
                break;
            }
            case Tokens.QUES:
            {
                res.which = 3;
                res.QUES = consume(Tokens.QUES, "QUES");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [QUES,STAR,PLUS] got: "+la);
            }
        }
        return res;
    }

    public Ast.simple simple(){
        Ast.simple res = new Ast.simple();
        switch(la.type){
            case Tokens.LP:
            {
                res.which = 1;
                res.group = group();
                break;
            }
            case Tokens.IDENT:
            case Tokens.OPTIONS:
            case Tokens.TOKEN:
            case Tokens.SKIP:
            case Tokens.INCLUDE:
            {
                res.which = 2;
                res.name = name();
                break;
            }
            case Tokens.CHAR:
            case Tokens.STRING:
            {
                res.which = 3;
                res.stringNode = stringNode();
                break;
            }
            case Tokens.BRACKET:
            {
                res.which = 4;
                res.bracketNode = bracketNode();
                break;
            }
            case Tokens.TILDE:
            {
                res.which = 5;
                res.untilNode = untilNode();
                break;
            }
            case Tokens.DOT:
            {
                res.which = 6;
                res.dotNode = dotNode();
                break;
            }
            case Tokens.EPSILON:
            {
                res.which = 7;
                res.EPSILON = consume(Tokens.EPSILON, "EPSILON");
                break;
            }
            case Tokens.LBRACE:
            {
                res.which = 8;
                res.repeatNode = repeatNode();
                break;
            }
            case Tokens.SHORTCUT:
            {
                res.which = 9;
                res.SHORTCUT = consume(Tokens.SHORTCUT, "SHORTCUT");
                break;
            }
            case Tokens.CALL_BEGIN:
            {
                res.which = 10;
                res.call = call();
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [CALL_BEGIN,OPTIONS,BRACKET,TOKEN,SKIP,TILDE,DOT,EPSILON,CHAR,LP,INCLUDE,IDENT,LBRACE,SHORTCUT,STRING] got: "+la);
            }
        }
        return res;
    }

    public Ast.simple simple_no_name(){
        Ast.simple res = new Ast.simple();
        switch(la.type){
            case Tokens.LP:
            {
                res.which = 1;
                res.group = group();
                break;
            }
            case Tokens.CHAR:
            case Tokens.STRING:
            {
                res.which = 3;
                res.stringNode = stringNode();
                break;
            }
            case Tokens.BRACKET:
            {
                res.which = 4;
                res.bracketNode = bracketNode();
                break;
            }
            case Tokens.TILDE:
            {
                res.which = 5;
                res.untilNode = untilNode();
                break;
            }
            case Tokens.DOT:
            {
                res.which = 6;
                res.dotNode = dotNode();
                break;
            }
            case Tokens.EPSILON:
            {
                res.which = 7;
                res.EPSILON = consume(Tokens.EPSILON, "EPSILON");
                break;
            }
            case Tokens.LBRACE:
            {
                res.which = 8;
                res.repeatNode = repeatNode();
                break;
            }
            case Tokens.SHORTCUT:
            {
                res.which = 9;
                res.SHORTCUT = consume(Tokens.SHORTCUT, "SHORTCUT");
                break;
            }
            case Tokens.CALL_BEGIN:
            {
                res.which = 10;
                res.call = call();
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [CALL_BEGIN,BRACKET,LBRACE,TILDE,DOT,SHORTCUT,EPSILON,CHAR,LP,STRING] got: "+la);
            }
        }
        return res;
    }

    public Ast.simple simple_name(Ast.name namef12){
        Ast.simple res = new Ast.simple();
        res.which = 2;
        res.name = namef12;
        return res;
    }

    public Ast.group group(){
        Ast.group res = new Ast.group();
        res.LP = consume(Tokens.LP, "LP");
        res.rhs = rhs();
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.stringNode stringNode(){
        Ast.stringNode res = new Ast.stringNode();
        switch(la.type){
            case Tokens.STRING:
            {
                res.which = 1;
                res.STRING = consume(Tokens.STRING, "STRING");
                break;
            }
            case Tokens.CHAR:
            {
                res.which = 2;
                res.CHAR = consume(Tokens.CHAR, "CHAR");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [CHAR,STRING] got: "+la);
            }
        }
        return res;
    }

    public Ast.bracketNode bracketNode(){
        Ast.bracketNode res = new Ast.bracketNode();
        res.BRACKET = consume(Tokens.BRACKET, "BRACKET");
        return res;
    }

    public Ast.untilNode untilNode(){
        Ast.untilNode res = new Ast.untilNode();
        res.TILDE = consume(Tokens.TILDE, "TILDE");
        res.regex = regex();
        return res;
    }

    public Ast.dotNode dotNode(){
        Ast.dotNode res = new Ast.dotNode();
        res.DOT = consume(Tokens.DOT, "DOT");
        return res;
    }

    public Ast.name name(){
        Ast.name res = new Ast.name();
        switch(la.type){
            case Tokens.IDENT:
            {
                res.which = 1;
                res.IDENT = consume(Tokens.IDENT, "IDENT");
                break;
            }
            case Tokens.TOKEN:
            {
                res.which = 2;
                res.TOKEN = consume(Tokens.TOKEN, "TOKEN");
                break;
            }
            case Tokens.SKIP:
            {
                res.which = 3;
                res.SKIP = consume(Tokens.SKIP, "SKIP");
                break;
            }
            case Tokens.OPTIONS:
            {
                res.which = 4;
                res.OPTIONS = consume(Tokens.OPTIONS, "OPTIONS");
                break;
            }
            case Tokens.INCLUDE:
            {
                res.which = 5;
                res.INCLUDE = consume(Tokens.INCLUDE, "INCLUDE");
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [IDENT,OPTIONS,TOKEN,SKIP,INCLUDE] got: "+la);
            }
        }
        return res;
    }

    public Ast.repeatNode repeatNode(){
        Ast.repeatNode res = new Ast.repeatNode();
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        res.rhs = rhs();
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }

    public Ast.call call(){
        Ast.call res = new Ast.call();
        res.CALL_BEGIN = consume(Tokens.CALL_BEGIN, "CALL_BEGIN");
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        while(la.type == Tokens.COMMA){
            res.g1.add(callg1());
        }
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.callg1 callg1(){
        Ast.callg1 res = new Ast.callg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        return res;
    }

    public Ast.join join(){
        Ast.join res = new Ast.join();
        res.JOIN = consume(Tokens.JOIN, "JOIN");
        res.LP = consume(Tokens.LP, "LP");
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }

    public Ast.nameOrString nameOrString(){
        Ast.nameOrString res = new Ast.nameOrString();
        switch(la.type){
            case Tokens.IDENT:
            case Tokens.OPTIONS:
            case Tokens.TOKEN:
            case Tokens.SKIP:
            case Tokens.INCLUDE:
            {
                res.which = 1;
                res.name = name();
                break;
            }
            case Tokens.CHAR:
            case Tokens.STRING:
            {
                res.which = 2;
                res.stringNode = stringNode();
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [IDENT,OPTIONS,TOKEN,SKIP,CHAR,STRING,INCLUDE] got: "+la);
            }
        }
        return res;
    }

}
