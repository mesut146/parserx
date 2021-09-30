package mesut.parserx.parser;

import java.util.List;
import java.util.ArrayList;
import mesut.parserx.parser.Ast;

public class Parser{
    List<Token> list = new ArrayList<>();
    Lexer lexer;

    public Parser(Lexer lexer) throws java.io.IOException{
        this.lexer = lexer;
        fill();
    }

    Token consume(int type){
        Token t = pop();
        if(t.type != type){
            throw new RuntimeException("unexpected token: " + t + " expecting: " + type);
        }
        return t;
    }
    Token pop(){
        return list.remove(0);
    }
    Token peek(){
        return list.get(0);
    }
    void fill() throws java.io.IOException{
        while(true){
            Token t = lexer.next();
            list.add(t);
            if(t == null || t.type == 0) return;
        }
    }
    public Ast.tree tree(){
        Ast.tree res = new Ast.tree();
        while(peek().type == Tokens.INCLUDE){
            res.includeStatement.add(includeStatement());
        }
        if(peek().type == Tokens.OPTIONS){
            res.optionsBlock = optionsBlock();
        }
        boolean flag = true;
        while(flag){
            switch(peek().type){
                case Tokens.TOKEN:
                case Tokens.SKIP:
                {
                    res.tokens.add(treeg1());
                }
                break;
                default:{
                    flag = false;
                }
            }
        }
        if(peek().type == Tokens.START){
            res.startDecl = startDecl();
        }
        boolean flag2 = true;
        while(flag2){
            switch(peek().type){
                case Tokens.IDENT:
                case Tokens.OPTIONS:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                case Tokens.LEFT:
                case Tokens.RIGHT:
                {
                    res.rules.add(treeg2());
                }
                break;
                default:{
                    flag2 = false;
                }
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
        while(peek().type == Tokens.IDENT){
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
        if(peek().type == Tokens.SEMI){
            res.SEMI = consume(Tokens.SEMI);
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
        boolean flag = true;
        while(flag){
            switch(peek().type){
                case Tokens.IDENT:
                case Tokens.OPTIONS:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                case Tokens.HASH:
                {
                    res.tokenDecl.add(tokenDecl());
                }
                break;
                default:{
                    flag = false;
                }
            }
        }
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

    public Ast.skipBlock skipBlock(){
        Ast.skipBlock res = new Ast.skipBlock();
        res.SKIP = consume(Tokens.SKIP);
        res.LBRACE = consume(Tokens.LBRACE);
        boolean flag = true;
        while(flag){
            switch(peek().type){
                case Tokens.IDENT:
                case Tokens.OPTIONS:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                case Tokens.HASH:
                {
                    res.tokenDecl.add(tokenDecl());
                }
                break;
                default:{
                    flag = false;
                }
            }
        }
        res.RBRACE = consume(Tokens.RBRACE);
        return res;
    }

    public Ast.tokenDecl tokenDecl(){
        Ast.tokenDecl res = new Ast.tokenDecl();
        if(peek().type == Tokens.HASH){
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
        if(peek().type == Tokens.LP){
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
        while(peek().type == Tokens.COMMA){
            res.rest.add(argsg1());
        }
        res.RP = consume(Tokens.RP);
        return res;
    }

    public Ast.assocDecl assocDecl(){
        Ast.assocDecl res = new Ast.assocDecl();
        res.type = assocDeclg1();
        boolean flag = true;
        boolean first = true;
        while(flag){
            switch(peek().type){
                case Tokens.IDENT:
                case Tokens.OPTIONS:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                {
                    res.ref.add(ref());
                }
                break;
                default:{
                    if(!first)  flag = false;
                    else  throw new RuntimeException("unexpected token: "+peek());
                }
            }
            first = false;

        }
        res.SEMI = consume(Tokens.SEMI);
        return res;
    }

    public Ast.rhs rhs(){
        Ast.rhs res = new Ast.rhs();
        res.sequence = sequence();
        while(peek().type == Tokens.OR){
            res.g1.add(rhsg1());
        }
        return res;
    }

    public Ast.sequence sequence(){
        Ast.sequence res = new Ast.sequence();
        boolean flag = true;
        boolean first = true;
        while(flag){
            switch(peek().type){
                case Tokens.OPTIONS:
                case Tokens.BRACKET:
                case Tokens.TOKEN:
                case Tokens.SKIP:
                case Tokens.TILDE:
                case Tokens.DOT:
                case Tokens.EPSILON:
                case Tokens.LP:
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
                    else  throw new RuntimeException("unexpected token: "+peek());
                }
            }
            first = false;

        }
        if(peek().type == Tokens.HASH){
            res.label = sequenceg1();
        }
        return res;
    }

    public Ast.regex regex(){
        Ast.regex res = new Ast.regex();
        switch(peek().type){
            case Tokens.IDENT:
            case Tokens.OPTIONS:
            case Tokens.TOKEN:
            case Tokens.SKIP:
            {
                Ast.name namef1 = name();
                switch(peek().type){
                    case Tokens.SEPARATOR:
                    {
                        res.name = regexg1(namef1);
                        res.simple = simple();
                        switch(peek().type){
                            case Tokens.QUES:
                            case Tokens.STAR:
                            case Tokens.PLUS:
                            {
                                res.type = regexg2();
                            }
                            break;
                        }
                        break;
                    }
                    case Tokens.QUES:
                    case Tokens.STAR:
                    case Tokens.PLUS:
                    {
                        res.simple = simple(namef1);
                        switch(peek().type){
                            case Tokens.QUES:
                            case Tokens.STAR:
                            case Tokens.PLUS:
                            {
                                res.type = regexg2();
                            }
                            break;
                        }
                        break;
                    }
                    default:{
                        res.simple = simple(namef1);
                        switch(peek().type){
                            case Tokens.QUES:
                            case Tokens.STAR:
                            case Tokens.PLUS:
                            {
                                res.type = regexg2();
                            }
                            break;
                        }
                    }
                }
                break;
            }
            case Tokens.BRACKET:
            case Tokens.TILDE:
            case Tokens.DOT:
            case Tokens.EPSILON:
            case Tokens.LP:
            case Tokens.LBRACE:
            case Tokens.SHORTCUT:
            case Tokens.STRING:
            {
                res.simple = simple_no_name();
                switch(peek().type){
                    case Tokens.QUES:
                    case Tokens.STAR:
                    case Tokens.PLUS:
                    {
                        res.type = regexg2();
                    }
                    break;
                }
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [OPTIONS = OPTIONS,BRACKET = BRACKET,TOKEN = TOKEN,bracketNode = bracketNode,SKIP = SKIP,TILDE = TILDE,DOT = DOT,simple = simple_no_name,EPSILON = EPSILON,group = group,stringNode = stringNode,LP = LP,untilNode = untilNode,name = name,IDENT = IDENT,dotNode = dotNode,LBRACE = LBRACE,SHORTCUT = SHORTCUT,STRING = STRING,repeatNode = repeatNode] got: "+peek());
            }
        }
        return res;
    }

    public Ast.simple simple(){
        Ast.simple res = new Ast.simple();
        switch(peek().type){
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
            {
                res.which = 2;
                res.ref = ref();
                break;
            }
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
                throw new RuntimeException("expecting one of [OPTIONS = OPTIONS,BRACKET = BRACKET,TOKEN = TOKEN,bracketNode = bracketNode,SKIP = SKIP,TILDE = TILDE,DOT = DOT,EPSILON = EPSILON,group = group,stringNode = stringNode,LP = LP,untilNode = untilNode,name = name,IDENT = IDENT,dotNode = dotNode,LBRACE = LBRACE,ref = ref,SHORTCUT = SHORTCUT,STRING = STRING,repeatNode = repeatNode] got: "+peek());
            }
        }
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
        res.STRING = consume(Tokens.STRING);
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

    public Ast.name name(){
        Ast.name res = new Ast.name();
        switch(peek().type){
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
            default:{
                throw new RuntimeException("expecting one of [IDENT = IDENT,OPTIONS = OPTIONS,TOKEN = TOKEN,SKIP = SKIP] got: "+peek());
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

    public Ast.treeg1 treeg1(){
        Ast.treeg1 res = new Ast.treeg1();
        switch(peek().type){
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
                throw new RuntimeException("expecting one of [TOKEN = TOKEN,SKIP = SKIP,skipBlock = skipBlock,tokenBlock = tokenBlock] got: "+peek());
            }
        }
        return res;
    }

    public Ast.treeg2 treeg2(){
        Ast.treeg2 res = new Ast.treeg2();
        switch(peek().type){
            case Tokens.IDENT:
            case Tokens.OPTIONS:
            case Tokens.TOKEN:
            case Tokens.SKIP:
            {
                res.which = 1;
                res.ruleDecl = ruleDecl();
                break;
            }
            case Tokens.LEFT:
            case Tokens.RIGHT:
            {
                res.which = 2;
                res.assocDecl = assocDecl();
                break;
            }
            default:{
                throw new RuntimeException("expecting one of [name = name,IDENT = IDENT,OPTIONS = OPTIONS,TOKEN = TOKEN,SKIP = SKIP,LEFT = LEFT,RIGHT = RIGHT,ruleDecl = ruleDecl,type = assocDeclg1,assocDecl = assocDecl] got: "+peek());
            }
        }
        return res;
    }

    public Ast.optiong1 optiong1(){
        Ast.optiong1 res = new Ast.optiong1();
        switch(peek().type){
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
                throw new RuntimeException("expecting one of [BOOLEAN = BOOLEAN,NUMBER = NUMBER] got: "+peek());
            }
        }
        return res;
    }

    public Ast.argsg1 argsg1(){
        Ast.argsg1 res = new Ast.argsg1();
        res.COMMA = consume(Tokens.COMMA);
        res.name = name();
        return res;
    }

    public Ast.assocDeclg1 assocDeclg1(){
        Ast.assocDeclg1 res = new Ast.assocDeclg1();
        switch(peek().type){
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
                throw new RuntimeException("expecting one of [LEFT = LEFT,RIGHT = RIGHT] got: "+peek());
            }
        }
        return res;
    }

    public Ast.rhsg1 rhsg1(){
        Ast.rhsg1 res = new Ast.rhsg1();
        res.OR = consume(Tokens.OR);
        res.sequence = sequence();
        return res;
    }

    public Ast.sequenceg1 sequenceg1(){
        Ast.sequenceg1 res = new Ast.sequenceg1();
        res.HASH = consume(Tokens.HASH);
        res.name = name();
        return res;
    }

    public Ast.regexg1 regexg1(){
        Ast.regexg1 res = new Ast.regexg1();
        res.name = name();
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        return res;
    }

    public Ast.regexg2 regexg2(){
        Ast.regexg2 res = new Ast.regexg2();
        switch(peek().type){
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
                throw new RuntimeException("expecting one of [QUES = QUES,STAR = STAR,PLUS = PLUS] got: "+peek());
            }
        }
        return res;
    }

    public Ast.regexg1 regexg1(Ast.name namef1){
        Ast.regexg1 res = new Ast.regexg1();
        res.name = namef1;
        res.SEPARATOR = consume(Tokens.SEPARATOR);
        return res;
    }

    public Ast.ref ref(Ast.name namef1){
        Ast.ref res = new Ast.ref();
        res.name = namef1;
        return res;
    }

    public Ast.simple simple(Ast.name namef1){
        Ast.simple res = new Ast.simple();
        res.which = 2;
        res.ref = ref(namef1);
        return res;
    }

    public Ast.simple simple_no_name(){
        Ast.simple res = new Ast.simple();
        switch(peek().type){
            case Tokens.LP:
            {
                res.which = 1;
                res.group = group();
                break;
            }
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
                throw new RuntimeException("expecting one of [BRACKET = BRACKET,bracketNode = bracketNode,TILDE = TILDE,DOT = DOT,EPSILON = EPSILON,group = group,stringNode = stringNode,LP = LP,untilNode = untilNode,dotNode = dotNode,LBRACE = LBRACE,SHORTCUT = SHORTCUT,STRING = STRING,repeatNode = repeatNode] got: "+peek());
            }
        }
        return res;
    }

}
