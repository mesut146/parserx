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
        res.INCLUDE = consume(Tokens.INCLUDE);
        res.STRING = consume(Tokens.STRING);
        return res;
    }

    public Ast.optionsBlock optionsBlock(){
        Ast.optionsBlock res = new Ast.optionsBlock();
        res.OPTIONS = consume(Tokens.OPTIONS);
        res.LBRACE = consume(Tokens.LBRACE);
        while(la.type == Tokens.IDENT){
            res.option.add(option());
        }
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

    public Ast.option option(){
        Ast.option res = new Ast.option();
        res.key = consume(Tokens.IDENT);
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        res.value = optiong1();
        if(la.type == Tokens.SEMI){
            res.SEMI = consume(Tokens.SEMI);
        }
        return res;
    }

    public Ast.optiong1 optiong1(){
        Ast.optiong1 res = new Ast.optiong1();
        switch(la.type){
            case Tokens.NUMBER:
            {
                res.which = 1;
                res.NUMBER = consume(Tokens.NUMBER);
                break;
            }
            case Tokens.BOOLEAN:
            {
                res.which = 2;
                res.BOOLEAN = consume(Tokens.BOOLEAN);
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
        res.START = consume(Tokens.START);
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        res.name = name();
        res.SEMI = consume(Tokens.SEMI);
        return res;
    }

    public Ast.tokenBlock tokenBlock(){
        Ast.tokenBlock res = new Ast.tokenBlock();
        res.TOKEN = consume(Tokens.TOKEN);
        res.LBRACE = consume(Tokens.LBRACE);
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.TOKEN || la.type == Tokens.SKIP || la.type == Tokens.HASH || la.type == Tokens.INCLUDE){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

    public Ast.skipBlock skipBlock(){
        Ast.skipBlock res = new Ast.skipBlock();
        res.SKIP = consume(Tokens.SKIP);
        res.LBRACE = consume(Tokens.LBRACE);
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.TOKEN || la.type == Tokens.SKIP || la.type == Tokens.HASH || la.type == Tokens.INCLUDE){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

    public Ast.tokenDecl tokenDecl(){
        Ast.tokenDecl res = new Ast.tokenDecl();
        if(la.type == Tokens.HASH){
            res.HASH = consume(Tokens.HASH);
        }
        res.name = name();
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        res.rhs = rhs();
        res.SEMI = consume(Tokens.SEMI);
        return res;
    }

    public Ast.ruleDecl ruleDecl(){
        Ast.ruleDecl res = new Ast.ruleDecl();
        res.name = name();
        if(la.type == Tokens.LP){
            res.args = args();
        }
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        res.rhs = rhs();
        res.SEMI = consume(Tokens.SEMI);
        return res;
    }

    public Ast.args args(){
        Ast.args res = new Ast.args();
        res.LP = consume(Tokens.LP);
        res.name = name();
        while(la.type == Tokens.COMMA){
            res.rest.add(argsg1());
        }
        res.RP = consume(Tokens.RP);
        return res;
    }

    public Ast.argsg1 argsg1(){
        Ast.argsg1 res = new Ast.argsg1();
        res.COMMA = consume(Tokens.COMMA);
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
        res.OR = consume(Tokens.OR);
        res.sequence = sequence();
        return res;
    }

    public Ast.sequence sequence(){
        Ast.sequence res = new Ast.sequence();
        boolean flag = true;
        boolean first = true;
        while(flag){
            switch(la.type){
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
        res.HASH = consume(Tokens.HASH);
        res.name = name();
        return res;
    }

    public Ast.sequenceg1 sequenceg1(){
        Ast.sequenceg1 res = new Ast.sequenceg1();
        switch(la.type){
            case Tokens.LEFT:
            {
                res.which = 1;
                res.LEFT = consume(Tokens.LEFT);
                break;
            }
            case Tokens.RIGHT:
            {
                res.which = 2;
                res.RIGHT = consume(Tokens.RIGHT);
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
                        regex1.SEPARATOR = consume(Tokens.SEPARATOR);
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
                        regex2.simple = simple1(namef1);
                        if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                            regex2.type = regexg2();
                        }
                        break;
                    }
                    default:{
                        res.which = 2;
                        Ast.regex.Regex2 regex2 = res.regex2 = new Ast.regex.Regex2();
                        regex2.simple = simple1(namef1);
                        if(la.type == Tokens.QUES || la.type == Tokens.STAR || la.type == Tokens.PLUS){
                            regex2.type = regexg2();
                        }
                    }
                }
                break;
            }
            case Tokens.BRACKET:
            case Tokens.TILDE:
            case Tokens.DOT:
            case Tokens.EPSILON:
            case Tokens.CHAR:
            case Tokens.LP:
            case Tokens.LBRACE:
            case Tokens.SHORTCUT:
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
                throw new RuntimeException("expecting one of [OPTIONS,BRACKET,TOKEN,SKIP,TILDE,DOT,EPSILON,CHAR,LP,INCLUDE,IDENT,LBRACE,SHORTCUT,STRING] got: "+la);
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
                res.STAR = consume(Tokens.STAR);
                break;
            }
            case Tokens.PLUS:
            {
                res.which = 2;
                res.PLUS = consume(Tokens.PLUS);
                break;
            }
            case Tokens.QUES:
            {
                res.which = 3;
                res.QUES = consume(Tokens.QUES);
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
                res.STAR = consume(Tokens.STAR);
                break;
            }
            case Tokens.PLUS:
            {
                res.which = 2;
                res.PLUS = consume(Tokens.PLUS);
                break;
            }
            case Tokens.QUES:
            {
                res.which = 3;
                res.QUES = consume(Tokens.QUES);
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
                res.ref = ref();
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
                res.EPSILON = consume(Tokens.EPSILON);
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
                res.SHORTCUT = consume(Tokens.SHORTCUT);
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [OPTIONS,BRACKET,TOKEN,SKIP,TILDE,DOT,EPSILON,CHAR,LP,INCLUDE,IDENT,LBRACE,SHORTCUT,STRING] got: "+la);
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
                res.EPSILON = consume(Tokens.EPSILON);
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
                res.SHORTCUT = consume(Tokens.SHORTCUT);
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [BRACKET,TILDE,DOT,EPSILON,CHAR,LP,LBRACE,SHORTCUT,STRING] got: "+la);
            }
        }
        return res;
    }

    public Ast.simple simple1(Ast.name namef1){
        Ast.simple res = new Ast.simple();
        res.which = 2;
        res.ref = ref1(namef1);
        return res;
    }

    public Ast.group group(){
        Ast.group res = new Ast.group();
        res.LP = consume(Tokens.LP);
        res.rhs = rhs();
        res.RP = consume(Tokens.RP);
        return res;
    }

    public Ast.stringNode stringNode(){
        Ast.stringNode res = new Ast.stringNode();
        switch(la.type){
            case Tokens.STRING:
            {
                res.which = 1;
                res.STRING = consume(Tokens.STRING);
                break;
            }
            case Tokens.CHAR:
            {
                res.which = 2;
                res.CHAR = consume(Tokens.CHAR);
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
        res.BRACKET = consume(Tokens.BRACKET);
        return res;
    }

    public Ast.untilNode untilNode(){
        Ast.untilNode res = new Ast.untilNode();
        res.TILDE = consume(Tokens.TILDE);
        res.regex = regex();
        return res;
    }

    public Ast.dotNode dotNode(){
        Ast.dotNode res = new Ast.dotNode();
        res.DOT = consume(Tokens.DOT);
        return res;
    }

    public Ast.ref ref(){
        Ast.ref res = new Ast.ref();
        res.name = name();
        return res;
    }

    public Ast.ref ref1(Ast.name namef1){
        Ast.ref res = new Ast.ref();
        res.name = namef1;
        return res;
    }

    public Ast.name name(){
        Ast.name res = new Ast.name();
        switch(la.type){
            case Tokens.IDENT:
            {
                res.which = 1;
                res.IDENT = consume(Tokens.IDENT);
                break;
            }
            case Tokens.TOKEN:
            {
                res.which = 2;
                res.TOKEN = consume(Tokens.TOKEN);
                break;
            }
            case Tokens.SKIP:
            {
                res.which = 3;
                res.SKIP = consume(Tokens.SKIP);
                break;
            }
            case Tokens.OPTIONS:
            {
                res.which = 4;
                res.OPTIONS = consume(Tokens.OPTIONS);
                break;
            }
            case Tokens.INCLUDE:
            {
                res.which = 5;
                res.INCLUDE = consume(Tokens.INCLUDE);
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
        res.LBRACE = consume(Tokens.LBRACE);
        res.rhs = rhs();
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

}
