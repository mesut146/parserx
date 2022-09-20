package mesut.parserx.parser;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

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
    public Ast.regex regex() throws IOException{
        if(la.type == Tokens.CALL_BEGIN){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple9 v3 = new Ast.simple.Simple9();
            v3.holder = v2;
            Ast.call v4 = new Ast.call();
            v1.simple = v2;
            v3.call = v4;
            v4.CALL_BEGIN = la;
            la = lexer.next();
            S9(v1, v3, v4);
            return v0;
        }
        else if(la.type == Tokens.OPTIONS){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex1 v1 = new Ast.regex.Regex1();
            v1.holder = v0;
            Ast.regex.Regex2 v2 = new Ast.regex.Regex2();
            v2.holder = v0;
            Ast.simple v3 = new Ast.simple();
            Ast.simple.Simple2 v4 = new Ast.simple.Simple2();
            v4.holder = v3;
            Ast.name v5 = new Ast.name();
            Ast.name.Name3 v6 = new Ast.name.Name3();
            v6.holder = v5;
            v2.simple = v3;
            v4.name = v5;
            v1.name = v5;
            v6.OPTIONS = la;
            la = lexer.next();
            S10(v1, v2, v4, v6);
            return v0;
        }
        else if(la.type == Tokens.BRACKET){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple4 v3 = new Ast.simple.Simple4();
            v3.holder = v2;
            Ast.bracketNode v4 = new Ast.bracketNode();
            v1.simple = v2;
            v3.bracketNode = v4;
            v4.BRACKET = la;
            la = lexer.next();
            S11(v1, v3, v4);
            return v0;
        }
        else if(la.type == Tokens.TOKEN){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex1 v1 = new Ast.regex.Regex1();
            v1.holder = v0;
            Ast.regex.Regex2 v2 = new Ast.regex.Regex2();
            v2.holder = v0;
            Ast.simple v3 = new Ast.simple();
            Ast.simple.Simple2 v4 = new Ast.simple.Simple2();
            v4.holder = v3;
            Ast.name v5 = new Ast.name();
            Ast.name.Name2 v6 = new Ast.name.Name2();
            v6.holder = v5;
            v2.simple = v3;
            v4.name = v5;
            v1.name = v5;
            v6.TOKEN = la;
            la = lexer.next();
            S12(v1, v2, v4, v6);
            return v0;
        }
        else if(la.type == Tokens.SKIP){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex1 v1 = new Ast.regex.Regex1();
            v1.holder = v0;
            Ast.regex.Regex2 v2 = new Ast.regex.Regex2();
            v2.holder = v0;
            Ast.simple v3 = new Ast.simple();
            Ast.simple.Simple2 v4 = new Ast.simple.Simple2();
            v4.holder = v3;
            Ast.name v5 = new Ast.name();
            Ast.name.Name4 v6 = new Ast.name.Name4();
            v6.holder = v5;
            v2.simple = v3;
            v4.name = v5;
            v1.name = v5;
            v6.SKIP = la;
            la = lexer.next();
            S13(v1, v2, v4, v6);
            return v0;
        }
        else if(la.type == Tokens.TILDE){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple5 v3 = new Ast.simple.Simple5();
            v3.holder = v2;
            Ast.untilNode v4 = new Ast.untilNode();
            v1.simple = v2;
            v3.untilNode = v4;
            v4.TILDE = la;
            la = lexer.next();
            S14(v1, v3, v4);
            return v0;
        }
        else if(la.type == Tokens.DOT){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple6 v3 = new Ast.simple.Simple6();
            v3.holder = v2;
            Ast.dotNode v4 = new Ast.dotNode();
            v1.simple = v2;
            v3.dotNode = v4;
            v4.DOT = la;
            la = lexer.next();
            S15(v1, v3, v4);
            return v0;
        }
        else if(la.type == Tokens.EPSILON){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple7 v3 = new Ast.simple.Simple7();
            v3.holder = v2;
            v1.simple = v2;
            v3.EPSILON = la;
            la = lexer.next();
            S16(v1, v3);
            return v0;
        }
        else if(la.type == Tokens.CHAR){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple3 v3 = new Ast.simple.Simple3();
            v3.holder = v2;
            Ast.stringNode v4 = new Ast.stringNode();
            Ast.stringNode.Stringnode2 v5 = new Ast.stringNode.Stringnode2();
            v5.holder = v4;
            v1.simple = v2;
            v3.stringNode = v4;
            v5.CHAR = la;
            la = lexer.next();
            S17(v1, v3, v5);
            return v0;
        }
        else if(la.type == Tokens.LP){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple1 v3 = new Ast.simple.Simple1();
            v3.holder = v2;
            Ast.group v4 = new Ast.group();
            v1.simple = v2;
            v3.group = v4;
            v4.LP = la;
            la = lexer.next();
            S18(v1, v3, v4);
            return v0;
        }
        else if(la.type == Tokens.IDENT){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex1 v1 = new Ast.regex.Regex1();
            v1.holder = v0;
            Ast.regex.Regex2 v2 = new Ast.regex.Regex2();
            v2.holder = v0;
            Ast.simple v3 = new Ast.simple();
            Ast.simple.Simple2 v4 = new Ast.simple.Simple2();
            v4.holder = v3;
            Ast.name v5 = new Ast.name();
            Ast.name.Name1 v6 = new Ast.name.Name1();
            v6.holder = v5;
            v2.simple = v3;
            v4.name = v5;
            v1.name = v5;
            v6.IDENT = la;
            la = lexer.next();
            S19(v1, v2, v4, v6);
            return v0;
        }
        else if(la.type == Tokens.SHORTCUT){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple8 v3 = new Ast.simple.Simple8();
            v3.holder = v2;
            v1.simple = v2;
            v3.SHORTCUT = la;
            la = lexer.next();
            S20(v1, v3);
            return v0;
        }
        else if(la.type == Tokens.STRING){
            Ast.regex v0 = new Ast.regex();
            Ast.regex.Regex2 v1 = new Ast.regex.Regex2();
            v1.holder = v0;
            Ast.simple v2 = new Ast.simple();
            Ast.simple.Simple3 v3 = new Ast.simple.Simple3();
            v3.holder = v2;
            Ast.stringNode v4 = new Ast.stringNode();
            Ast.stringNode.Stringnode1 v5 = new Ast.stringNode.Stringnode1();
            v5.holder = v4;
            v1.simple = v2;
            v3.stringNode = v4;
            v5.STRING = la;
            la = lexer.next();
            S21(v1, v3, v5);
            return v0;
        }
        else throw new RuntimeException("expecting one of [CALL_BEGIN, OPTIONS, BRACKET, TOKEN, SKIP, TILDE, DOT, EPSILON, CHAR, LP, IDENT, SHORTCUT, STRING] got: "+la);
    }
    public void S9(Ast.regex.Regex2 p0, Ast.simple.Simple9 p1, Ast.call p2) throws IOException{
        if(la.type == Tokens.IDENT){
            p2.IDENT = consume(Tokens.IDENT, "IDENT");
            while(la.type == Tokens.COMMA){
                p2.g1.add(callg1());
            }
            p2.RP = consume(Tokens.RP, "RP");
            S22(p0, p1, p2);
        }
    }
    public void S10(Ast.regex.Regex1 p0, Ast.regex.Regex2 p1, Ast.simple.Simple2 p2, Ast.name.Name3 p3) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 2;
            p2.holder.name = p2;
            p1.holder.which = 2;
            p1.holder.regex2 = p1;
        }
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SEPARATOR || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p3.holder.which = 3;
            p3.holder.OPTIONS = p3;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p1.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p1.holder.which = 2;
                p1.holder.regex2 = p1;
            }
        }
        else if(la.type == Tokens.SEPARATOR){
            p0.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p0.simple = simple();
            if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
                p0.type = regexg1();
            }
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.regex1 = p0;
            }
        }
    }
    public void S11(Ast.regex.Regex2 p0, Ast.simple.Simple4 p1, Ast.bracketNode p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 4;
            p1.holder.bracketNode = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S12(Ast.regex.Regex1 p0, Ast.regex.Regex2 p1, Ast.simple.Simple2 p2, Ast.name.Name2 p3) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 2;
            p2.holder.name = p2;
            p1.holder.which = 2;
            p1.holder.regex2 = p1;
        }
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SEPARATOR || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p3.holder.which = 2;
            p3.holder.TOKEN = p3;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p1.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p1.holder.which = 2;
                p1.holder.regex2 = p1;
            }
        }
        else if(la.type == Tokens.SEPARATOR){
            p0.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p0.simple = simple();
            if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
                p0.type = regexg1();
            }
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.regex1 = p0;
            }
        }
    }
    public void S13(Ast.regex.Regex1 p0, Ast.regex.Regex2 p1, Ast.simple.Simple2 p2, Ast.name.Name4 p3) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 2;
            p2.holder.name = p2;
            p1.holder.which = 2;
            p1.holder.regex2 = p1;
        }
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SEPARATOR || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p3.holder.which = 4;
            p3.holder.SKIP = p3;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p1.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p1.holder.which = 2;
                p1.holder.regex2 = p1;
            }
        }
        else if(la.type == Tokens.SEPARATOR){
            p0.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p0.simple = simple();
            if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
                p0.type = regexg1();
            }
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.regex1 = p0;
            }
        }
    }
    public void S14(Ast.regex.Regex2 p0, Ast.simple.Simple5 p1, Ast.untilNode p2) throws IOException{
        if(la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.IDENT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.regex = regex();
            S25(p0, p1, p2);
        }
    }
    public void S15(Ast.regex.Regex2 p0, Ast.simple.Simple6 p1, Ast.dotNode p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 6;
            p1.holder.dotNode = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S16(Ast.regex.Regex2 p0, Ast.simple.Simple7 p1) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 7;
            p1.holder.EPSILON = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S17(Ast.regex.Regex2 p0, Ast.simple.Simple3 p1, Ast.stringNode.Stringnode2 p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 2;
            p2.holder.CHAR = p2;
            p1.holder.which = 3;
            p1.holder.stringNode = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S18(Ast.regex.Regex2 p0, Ast.simple.Simple1 p1, Ast.group p2) throws IOException{
        if(la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.IDENT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.rhs = rhs();
            p2.RP = consume(Tokens.RP, "RP");
            S26(p0, p1, p2);
        }
    }
    public void S19(Ast.regex.Regex1 p0, Ast.regex.Regex2 p1, Ast.simple.Simple2 p2, Ast.name.Name1 p3) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 2;
            p2.holder.name = p2;
            p1.holder.which = 2;
            p1.holder.regex2 = p1;
        }
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SEPARATOR || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p3.holder.which = 1;
            p3.holder.IDENT = p3;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p1.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p1.holder.which = 2;
                p1.holder.regex2 = p1;
            }
        }
        else if(la.type == Tokens.SEPARATOR){
            p0.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p0.simple = simple();
            if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
                p0.type = regexg1();
            }
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.regex1 = p0;
            }
        }
    }
    public void S20(Ast.regex.Regex2 p0, Ast.simple.Simple8 p1) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 8;
            p1.holder.SHORTCUT = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S21(Ast.regex.Regex2 p0, Ast.simple.Simple3 p1, Ast.stringNode.Stringnode1 p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p2.holder.which = 1;
            p2.holder.STRING = p2;
            p1.holder.which = 3;
            p1.holder.stringNode = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S22(Ast.regex.Regex2 p0, Ast.simple.Simple9 p1, Ast.call p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 9;
            p1.holder.call = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S25(Ast.regex.Regex2 p0, Ast.simple.Simple5 p1, Ast.untilNode p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 5;
            p1.holder.untilNode = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public void S26(Ast.regex.Regex2 p0, Ast.simple.Simple1 p1, Ast.group p2) throws IOException{
        if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
            p1.holder.which = 1;
            p1.holder.group = p1;
            p0.holder.which = 2;
            p0.holder.regex2 = p0;
        }
        if(la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.STAR){
            p0.type = regexg2();
            if(la.type == Tokens.ARROW || la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.LEFT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.OR || la.type == Tokens.PLUS || la.type == Tokens.QUES || la.type == Tokens.RIGHT || la.type == Tokens.RP || la.type == Tokens.SEMI || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STAR || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN){
                p0.holder.which = 2;
                p0.holder.regex2 = p0;
            }
        }
    }
    public Ast.tokenBlockg1 tokenBlockg1() throws IOException{
        if(la.type == Tokens.IDENT){
            Ast.tokenBlockg1 v0 = new Ast.tokenBlockg1();
            Ast.tokenBlockg1.Tokenblockg11 v1 = new Ast.tokenBlockg1.Tokenblockg11();
            v1.holder = v0;
            Ast.tokenBlockg1.Tokenblockg12 v2 = new Ast.tokenBlockg1.Tokenblockg12();
            v2.holder = v0;
            Ast.tokenDecl v3 = new Ast.tokenDecl();
            Ast.name v4 = new Ast.name();
            Ast.name.Name1 v5 = new Ast.name.Name1();
            v5.holder = v4;
            Ast.modeBlock v6 = new Ast.modeBlock();
            v1.tokenDecl = v3;
            v3.name = v4;
            v2.modeBlock = v6;
            v5.IDENT = la;
            v6.IDENT = la;
            la = lexer.next();
            S1(v1, v2, v3, v5, v6);
            return v0;
        }
        else if(la.type == Tokens.OPTIONS){
            Ast.tokenBlockg1 v0 = new Ast.tokenBlockg1();
            Ast.tokenBlockg1.Tokenblockg11 v1 = new Ast.tokenBlockg1.Tokenblockg11();
            v1.holder = v0;
            Ast.tokenDecl v2 = new Ast.tokenDecl();
            Ast.name v3 = new Ast.name();
            Ast.name.Name3 v4 = new Ast.name.Name3();
            v4.holder = v3;
            v1.tokenDecl = v2;
            v2.name = v3;
            v4.OPTIONS = la;
            la = lexer.next();
            S2(v1, v2, v4);
            return v0;
        }
        else if(la.type == Tokens.TOKEN){
            Ast.tokenBlockg1 v0 = new Ast.tokenBlockg1();
            Ast.tokenBlockg1.Tokenblockg11 v1 = new Ast.tokenBlockg1.Tokenblockg11();
            v1.holder = v0;
            Ast.tokenDecl v2 = new Ast.tokenDecl();
            Ast.name v3 = new Ast.name();
            Ast.name.Name2 v4 = new Ast.name.Name2();
            v4.holder = v3;
            v1.tokenDecl = v2;
            v2.name = v3;
            v4.TOKEN = la;
            la = lexer.next();
            S3(v1, v2, v4);
            return v0;
        }
        else if(la.type == Tokens.SKIP){
            Ast.tokenBlockg1 v0 = new Ast.tokenBlockg1();
            Ast.tokenBlockg1.Tokenblockg11 v1 = new Ast.tokenBlockg1.Tokenblockg11();
            v1.holder = v0;
            Ast.tokenDecl v2 = new Ast.tokenDecl();
            Ast.name v3 = new Ast.name();
            Ast.name.Name4 v4 = new Ast.name.Name4();
            v4.holder = v3;
            v1.tokenDecl = v2;
            v2.name = v3;
            v4.SKIP = la;
            la = lexer.next();
            S4(v1, v2, v4);
            return v0;
        }
        else if(la.type == Tokens.HASH){
            Ast.tokenBlockg1 v0 = new Ast.tokenBlockg1();
            Ast.tokenBlockg1.Tokenblockg11 v1 = new Ast.tokenBlockg1.Tokenblockg11();
            v1.holder = v0;
            Ast.tokenDecl v2 = new Ast.tokenDecl();
            v1.tokenDecl = v2;
            v2.HASH = la;
            la = lexer.next();
            S5(v1, v2);
            return v0;
        }
        else throw new RuntimeException("expecting one of [IDENT, OPTIONS, TOKEN, SKIP, HASH] got: "+la);
    }
    public void S1(Ast.tokenBlockg1.Tokenblockg11 p0, Ast.tokenBlockg1.Tokenblockg12 p1, Ast.tokenDecl p2, Ast.name.Name1 p3, Ast.modeBlock p4) throws IOException{
        if(la.type == Tokens.SEPARATOR){
            p3.holder.which = 1;
            p3.holder.IDENT = p3;
        }
        if(la.type == Tokens.SEPARATOR){
            p2.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p2.rhs = rhs();
            if(la.type == Tokens.ARROW){
                p2.mode = tokenDeclg1();
            }
            p2.SEMI = consume(Tokens.SEMI, "SEMI");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.tokenDecl = p0;
            }
        }
        else if(la.type == Tokens.LBRACE){
            p4.LBRACE = consume(Tokens.LBRACE, "LBRACE");
            while(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p4.tokenDecl.add(tokenDecl());
            }
            p4.RBRACE = consume(Tokens.RBRACE, "RBRACE");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p1.holder.which = 2;
                p1.holder.modeBlock = p1;
            }
        }
    }
    public void S2(Ast.tokenBlockg1.Tokenblockg11 p0, Ast.tokenDecl p1, Ast.name.Name3 p2) throws IOException{
        if(la.type == Tokens.SEPARATOR){
            p2.holder.which = 3;
            p2.holder.OPTIONS = p2;
        }
        if(la.type == Tokens.SEPARATOR){
            p1.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p1.rhs = rhs();
            if(la.type == Tokens.ARROW){
                p1.mode = tokenDeclg1();
            }
            p1.SEMI = consume(Tokens.SEMI, "SEMI");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.tokenDecl = p0;
            }
        }
    }
    public void S3(Ast.tokenBlockg1.Tokenblockg11 p0, Ast.tokenDecl p1, Ast.name.Name2 p2) throws IOException{
        if(la.type == Tokens.SEPARATOR){
            p2.holder.which = 2;
            p2.holder.TOKEN = p2;
        }
        if(la.type == Tokens.SEPARATOR){
            p1.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p1.rhs = rhs();
            if(la.type == Tokens.ARROW){
                p1.mode = tokenDeclg1();
            }
            p1.SEMI = consume(Tokens.SEMI, "SEMI");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.tokenDecl = p0;
            }
        }
    }
    public void S4(Ast.tokenBlockg1.Tokenblockg11 p0, Ast.tokenDecl p1, Ast.name.Name4 p2) throws IOException{
        if(la.type == Tokens.SEPARATOR){
            p2.holder.which = 4;
            p2.holder.SKIP = p2;
        }
        if(la.type == Tokens.SEPARATOR){
            p1.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p1.rhs = rhs();
            if(la.type == Tokens.ARROW){
                p1.mode = tokenDeclg1();
            }
            p1.SEMI = consume(Tokens.SEMI, "SEMI");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.tokenDecl = p0;
            }
        }
    }
    public void S5(Ast.tokenBlockg1.Tokenblockg11 p0, Ast.tokenDecl p1) throws IOException{
        if(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            p1.name = name();
            p1.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
            p1.rhs = rhs();
            if(la.type == Tokens.ARROW){
                p1.mode = tokenDeclg1();
            }
            p1.SEMI = consume(Tokens.SEMI, "SEMI");
            if(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.RBRACE || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
                p0.holder.which = 1;
                p0.holder.tokenDecl = p0;
            }
        }
    }
    public Ast.tree tree() throws IOException{
        Ast.tree res = new Ast.tree();
        while(la.type == Tokens.INCLUDE){
            res.includeStatement.add(includeStatement());
        }
        if(la.type == Tokens.OPTIONS){
            res.optionsBlock = optionsBlock();
        }
        while(la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            res.tokens.add(treeg1());
        }
        if(la.type == Tokens.START){
            res.startDecl = startDecl();
        }
        while(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            res.rules.add(ruleDecl());
        }
        return res;
    }
    public Ast.treeg1 treeg1() throws IOException{
        Ast.treeg1 res = new Ast.treeg1();
        if(la.type == Tokens.TOKEN){
            Ast.treeg1.Treeg11 tokenBlock = new Ast.treeg1.Treeg11();
            tokenBlock.holder = res;
            res.tokenBlock = tokenBlock;
            res.which = 1;
            tokenBlock.tokenBlock = tokenBlock();
        }
        else if(la.type == Tokens.SKIP){
            Ast.treeg1.Treeg12 skipBlock = new Ast.treeg1.Treeg12();
            skipBlock.holder = res;
            res.skipBlock = skipBlock;
            res.which = 2;
            skipBlock.skipBlock = skipBlock();
        }
        else throw new RuntimeException("expecting one of [SKIP, TOKEN] got: "+la);
        return res;
    }
    public Ast.includeStatement includeStatement() throws IOException{
        Ast.includeStatement res = new Ast.includeStatement();
        res.INCLUDE = consume(Tokens.INCLUDE, "INCLUDE");
        res.STRING = consume(Tokens.STRING, "STRING");
        return res;
    }
    public Ast.optionsBlock optionsBlock() throws IOException{
        Ast.optionsBlock res = new Ast.optionsBlock();
        res.OPTIONS = consume(Tokens.OPTIONS, "OPTIONS");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.IDENT){
            res.option.add(option());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }
    public Ast.option option() throws IOException{
        Ast.option res = new Ast.option();
        res.key = consume(Tokens.IDENT, "IDENT");
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.value = optiong1();
        if(la.type == Tokens.SEMI){
            res.SEMI = consume(Tokens.SEMI, "SEMI");
        }
        return res;
    }
    public Ast.optiong1 optiong1() throws IOException{
        Ast.optiong1 res = new Ast.optiong1();
        if(la.type == Tokens.NUMBER){
            Ast.optiong1.Optiong11 NUMBER = new Ast.optiong1.Optiong11();
            NUMBER.holder = res;
            res.NUMBER = NUMBER;
            res.which = 1;
            NUMBER.NUMBER = consume(Tokens.NUMBER, "NUMBER");
        }
        else if(la.type == Tokens.BOOLEAN){
            Ast.optiong1.Optiong12 BOOLEAN = new Ast.optiong1.Optiong12();
            BOOLEAN.holder = res;
            res.BOOLEAN = BOOLEAN;
            res.which = 2;
            BOOLEAN.BOOLEAN = consume(Tokens.BOOLEAN, "BOOLEAN");
        }
        else throw new RuntimeException("expecting one of [BOOLEAN, NUMBER] got: "+la);
        return res;
    }
    public Ast.startDecl startDecl() throws IOException{
        Ast.startDecl res = new Ast.startDecl();
        res.START = consume(Tokens.START, "START");
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.name = name();
        res.SEMI = consume(Tokens.SEMI, "SEMI");
        return res;
    }
    public Ast.tokenBlock tokenBlock() throws IOException{
        Ast.tokenBlock res = new Ast.tokenBlock();
        res.TOKEN = consume(Tokens.TOKEN, "TOKEN");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            res.g1.add(tokenBlockg1());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }
    public Ast.tokenDecl tokenDecl() throws IOException{
        Ast.tokenDecl res = new Ast.tokenDecl();
        if(la.type == Tokens.HASH){
            res.HASH = consume(Tokens.HASH, "HASH");
        }
        res.name = name();
        res.SEPARATOR = consume(Tokens.SEPARATOR, "SEPARATOR");
        res.rhs = rhs();
        if(la.type == Tokens.ARROW){
            res.mode = tokenDeclg1();
        }
        res.SEMI = consume(Tokens.SEMI, "SEMI");
        return res;
    }
    public Ast.tokenDeclg1 tokenDeclg1() throws IOException{
        Ast.tokenDeclg1 res = new Ast.tokenDeclg1();
        res.ARROW = consume(Tokens.ARROW, "ARROW");
        res.modes = modes();
        return res;
    }
    public Ast.modes modes() throws IOException{
        Ast.modes res = new Ast.modes();
        res.name = name();
        if(la.type == Tokens.COMMA){
            res.g1 = modesg1();
        }
        return res;
    }
    public Ast.modesg1 modesg1() throws IOException{
        Ast.modesg1 res = new Ast.modesg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.name = name();
        return res;
    }
    public Ast.modeBlock modeBlock() throws IOException{
        Ast.modeBlock res = new Ast.modeBlock();
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }
    public Ast.skipBlock skipBlock() throws IOException{
        Ast.skipBlock res = new Ast.skipBlock();
        res.SKIP = consume(Tokens.SKIP, "SKIP");
        res.LBRACE = consume(Tokens.LBRACE, "LBRACE");
        while(la.type == Tokens.HASH || la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            res.tokenDecl.add(tokenDecl());
        }
        res.RBRACE = consume(Tokens.RBRACE, "RBRACE");
        return res;
    }
    public Ast.ruleDecl ruleDecl() throws IOException{
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
    public Ast.args args() throws IOException{
        Ast.args res = new Ast.args();
        res.LP = consume(Tokens.LP, "LP");
        res.name = name();
        while(la.type == Tokens.COMMA){
            res.rest.add(argsg1());
        }
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }
    public Ast.argsg1 argsg1() throws IOException{
        Ast.argsg1 res = new Ast.argsg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.name = name();
        return res;
    }
    public Ast.rhs rhs() throws IOException{
        Ast.rhs res = new Ast.rhs();
        res.sequence = sequence();
        while(la.type == Tokens.OR){
            res.g1.add(rhsg1());
        }
        return res;
    }
    public Ast.rhsg1 rhsg1() throws IOException{
        Ast.rhsg1 res = new Ast.rhsg1();
        res.OR = consume(Tokens.OR, "OR");
        res.sequence = sequence();
        return res;
    }
    public Ast.sequence sequence() throws IOException{
        Ast.sequence res = new Ast.sequence();
        do{
            res.regex.add(regex());
        }while(la.type == Tokens.BRACKET || la.type == Tokens.CALL_BEGIN || la.type == Tokens.CHAR || la.type == Tokens.DOT || la.type == Tokens.EPSILON || la.type == Tokens.IDENT || la.type == Tokens.LP || la.type == Tokens.OPTIONS || la.type == Tokens.SHORTCUT || la.type == Tokens.SKIP || la.type == Tokens.STRING || la.type == Tokens.TILDE || la.type == Tokens.TOKEN);
        if(la.type == Tokens.LEFT || la.type == Tokens.RIGHT){
            res.assoc = sequenceg1();
        }
        if(la.type == Tokens.HASH){
            res.label = sequenceg2();
        }
        return res;
    }
    public Ast.sequenceg2 sequenceg2() throws IOException{
        Ast.sequenceg2 res = new Ast.sequenceg2();
        res.HASH = consume(Tokens.HASH, "HASH");
        res.name = name();
        return res;
    }
    public Ast.sequenceg1 sequenceg1() throws IOException{
        Ast.sequenceg1 res = new Ast.sequenceg1();
        if(la.type == Tokens.LEFT){
            Ast.sequenceg1.Sequenceg11 LEFT = new Ast.sequenceg1.Sequenceg11();
            LEFT.holder = res;
            res.LEFT = LEFT;
            res.which = 1;
            LEFT.LEFT = consume(Tokens.LEFT, "LEFT");
        }
        else if(la.type == Tokens.RIGHT){
            Ast.sequenceg1.Sequenceg12 RIGHT = new Ast.sequenceg1.Sequenceg12();
            RIGHT.holder = res;
            res.RIGHT = RIGHT;
            res.which = 2;
            RIGHT.RIGHT = consume(Tokens.RIGHT, "RIGHT");
        }
        else throw new RuntimeException("expecting one of [LEFT, RIGHT] got: "+la);
        return res;
    }
    public Ast.regexg2 regexg2() throws IOException{
        Ast.regexg2 res = new Ast.regexg2();
        if(la.type == Tokens.STAR){
            Ast.regexg2.Regexg21 STAR = new Ast.regexg2.Regexg21();
            STAR.holder = res;
            res.STAR = STAR;
            res.which = 1;
            STAR.STAR = consume(Tokens.STAR, "STAR");
        }
        else if(la.type == Tokens.PLUS){
            Ast.regexg2.Regexg22 PLUS = new Ast.regexg2.Regexg22();
            PLUS.holder = res;
            res.PLUS = PLUS;
            res.which = 2;
            PLUS.PLUS = consume(Tokens.PLUS, "PLUS");
        }
        else if(la.type == Tokens.QUES){
            Ast.regexg2.Regexg23 QUES = new Ast.regexg2.Regexg23();
            QUES.holder = res;
            res.QUES = QUES;
            res.which = 3;
            QUES.QUES = consume(Tokens.QUES, "QUES");
        }
        else throw new RuntimeException("expecting one of [PLUS, QUES, STAR] got: "+la);
        return res;
    }
    public Ast.regexg1 regexg1() throws IOException{
        Ast.regexg1 res = new Ast.regexg1();
        if(la.type == Tokens.STAR){
            Ast.regexg1.Regexg11 STAR = new Ast.regexg1.Regexg11();
            STAR.holder = res;
            res.STAR = STAR;
            res.which = 1;
            STAR.STAR = consume(Tokens.STAR, "STAR");
        }
        else if(la.type == Tokens.PLUS){
            Ast.regexg1.Regexg12 PLUS = new Ast.regexg1.Regexg12();
            PLUS.holder = res;
            res.PLUS = PLUS;
            res.which = 2;
            PLUS.PLUS = consume(Tokens.PLUS, "PLUS");
        }
        else if(la.type == Tokens.QUES){
            Ast.regexg1.Regexg13 QUES = new Ast.regexg1.Regexg13();
            QUES.holder = res;
            res.QUES = QUES;
            res.which = 3;
            QUES.QUES = consume(Tokens.QUES, "QUES");
        }
        else throw new RuntimeException("expecting one of [PLUS, QUES, STAR] got: "+la);
        return res;
    }
    public Ast.simple simple() throws IOException{
        Ast.simple res = new Ast.simple();
        if(la.type == Tokens.LP){
            Ast.simple.Simple1 group = new Ast.simple.Simple1();
            group.holder = res;
            res.group = group;
            res.which = 1;
            group.group = group();
        }
        else if(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            Ast.simple.Simple2 name = new Ast.simple.Simple2();
            name.holder = res;
            res.name = name;
            res.which = 2;
            name.name = name();
        }
        else if(la.type == Tokens.CHAR || la.type == Tokens.STRING){
            Ast.simple.Simple3 stringNode = new Ast.simple.Simple3();
            stringNode.holder = res;
            res.stringNode = stringNode;
            res.which = 3;
            stringNode.stringNode = stringNode();
        }
        else if(la.type == Tokens.BRACKET){
            Ast.simple.Simple4 bracketNode = new Ast.simple.Simple4();
            bracketNode.holder = res;
            res.bracketNode = bracketNode;
            res.which = 4;
            bracketNode.bracketNode = bracketNode();
        }
        else if(la.type == Tokens.TILDE){
            Ast.simple.Simple5 untilNode = new Ast.simple.Simple5();
            untilNode.holder = res;
            res.untilNode = untilNode;
            res.which = 5;
            untilNode.untilNode = untilNode();
        }
        else if(la.type == Tokens.DOT){
            Ast.simple.Simple6 dotNode = new Ast.simple.Simple6();
            dotNode.holder = res;
            res.dotNode = dotNode;
            res.which = 6;
            dotNode.dotNode = dotNode();
        }
        else if(la.type == Tokens.EPSILON){
            Ast.simple.Simple7 EPSILON = new Ast.simple.Simple7();
            EPSILON.holder = res;
            res.EPSILON = EPSILON;
            res.which = 7;
            EPSILON.EPSILON = consume(Tokens.EPSILON, "EPSILON");
        }
        else if(la.type == Tokens.SHORTCUT){
            Ast.simple.Simple8 SHORTCUT = new Ast.simple.Simple8();
            SHORTCUT.holder = res;
            res.SHORTCUT = SHORTCUT;
            res.which = 8;
            SHORTCUT.SHORTCUT = consume(Tokens.SHORTCUT, "SHORTCUT");
        }
        else if(la.type == Tokens.CALL_BEGIN){
            Ast.simple.Simple9 call = new Ast.simple.Simple9();
            call.holder = res;
            res.call = call;
            res.which = 9;
            call.call = call();
        }
        else throw new RuntimeException("expecting one of [BRACKET, CALL_BEGIN, CHAR, DOT, EPSILON, IDENT, LP, OPTIONS, SHORTCUT, SKIP, STRING, TILDE, TOKEN] got: "+la);
        return res;
    }
    public Ast.group group() throws IOException{
        Ast.group res = new Ast.group();
        res.LP = consume(Tokens.LP, "LP");
        res.rhs = rhs();
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }
    public Ast.stringNode stringNode() throws IOException{
        Ast.stringNode res = new Ast.stringNode();
        if(la.type == Tokens.STRING){
            Ast.stringNode.Stringnode1 STRING = new Ast.stringNode.Stringnode1();
            STRING.holder = res;
            res.STRING = STRING;
            res.which = 1;
            STRING.STRING = consume(Tokens.STRING, "STRING");
        }
        else if(la.type == Tokens.CHAR){
            Ast.stringNode.Stringnode2 CHAR = new Ast.stringNode.Stringnode2();
            CHAR.holder = res;
            res.CHAR = CHAR;
            res.which = 2;
            CHAR.CHAR = consume(Tokens.CHAR, "CHAR");
        }
        else throw new RuntimeException("expecting one of [CHAR, STRING] got: "+la);
        return res;
    }
    public Ast.bracketNode bracketNode() throws IOException{
        Ast.bracketNode res = new Ast.bracketNode();
        res.BRACKET = consume(Tokens.BRACKET, "BRACKET");
        return res;
    }
    public Ast.untilNode untilNode() throws IOException{
        Ast.untilNode res = new Ast.untilNode();
        res.TILDE = consume(Tokens.TILDE, "TILDE");
        res.regex = regex();
        return res;
    }
    public Ast.dotNode dotNode() throws IOException{
        Ast.dotNode res = new Ast.dotNode();
        res.DOT = consume(Tokens.DOT, "DOT");
        return res;
    }
    public Ast.name name() throws IOException{
        Ast.name res = new Ast.name();
        if(la.type == Tokens.IDENT){
            Ast.name.Name1 IDENT = new Ast.name.Name1();
            IDENT.holder = res;
            res.IDENT = IDENT;
            res.which = 1;
            IDENT.IDENT = consume(Tokens.IDENT, "IDENT");
        }
        else if(la.type == Tokens.TOKEN){
            Ast.name.Name2 TOKEN = new Ast.name.Name2();
            TOKEN.holder = res;
            res.TOKEN = TOKEN;
            res.which = 2;
            TOKEN.TOKEN = consume(Tokens.TOKEN, "TOKEN");
        }
        else if(la.type == Tokens.OPTIONS){
            Ast.name.Name3 OPTIONS = new Ast.name.Name3();
            OPTIONS.holder = res;
            res.OPTIONS = OPTIONS;
            res.which = 3;
            OPTIONS.OPTIONS = consume(Tokens.OPTIONS, "OPTIONS");
        }
        else if(la.type == Tokens.SKIP){
            Ast.name.Name4 SKIP = new Ast.name.Name4();
            SKIP.holder = res;
            res.SKIP = SKIP;
            res.which = 4;
            SKIP.SKIP = consume(Tokens.SKIP, "SKIP");
        }
        else throw new RuntimeException("expecting one of [IDENT, OPTIONS, SKIP, TOKEN] got: "+la);
        return res;
    }
    public Ast.call call() throws IOException{
        Ast.call res = new Ast.call();
        res.CALL_BEGIN = consume(Tokens.CALL_BEGIN, "CALL_BEGIN");
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        while(la.type == Tokens.COMMA){
            res.g1.add(callg1());
        }
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }
    public Ast.callg1 callg1() throws IOException{
        Ast.callg1 res = new Ast.callg1();
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.IDENT = consume(Tokens.IDENT, "IDENT");
        return res;
    }
    public Ast.join join() throws IOException{
        Ast.join res = new Ast.join();
        res.JOIN = consume(Tokens.JOIN, "JOIN");
        res.LP = consume(Tokens.LP, "LP");
        res.COMMA = consume(Tokens.COMMA, "COMMA");
        res.RP = consume(Tokens.RP, "RP");
        return res;
    }
    public Ast.nameOrString nameOrString() throws IOException{
        Ast.nameOrString res = new Ast.nameOrString();
        if(la.type == Tokens.IDENT || la.type == Tokens.OPTIONS || la.type == Tokens.SKIP || la.type == Tokens.TOKEN){
            Ast.nameOrString.Nameorstring1 name = new Ast.nameOrString.Nameorstring1();
            name.holder = res;
            res.name = name;
            res.which = 1;
            name.name = name();
        }
        else if(la.type == Tokens.CHAR || la.type == Tokens.STRING){
            Ast.nameOrString.Nameorstring2 stringNode = new Ast.nameOrString.Nameorstring2();
            stringNode.holder = res;
            res.stringNode = stringNode;
            res.which = 2;
            stringNode.stringNode = stringNode();
        }
        else throw new RuntimeException("expecting one of [CHAR, IDENT, OPTIONS, SKIP, STRING, TOKEN] got: "+la);
        return res;
    }
}

