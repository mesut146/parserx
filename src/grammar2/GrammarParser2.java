package grammar2;

import java.util.*;
import nodes.*;
import rule.*;
import java.io.*;
import static grammar2.TokenType.*;

public class GrammarParser2
{
    List<TokenDecl> tokens=new ArrayList<>();
    List<RuleDecl> rules=new ArrayList<>();
    Stack<Node> stack=new Stack<>();
    GrammarLexer lexer;
    
    public GrammarParser2(GrammarLexer lexer){
        this.lexer=lexer;
    }
    
    GrammarToken next() throws IOException {
        return lexer.nextToken();
    }
    
    
    void shift() throws Exception{
        stack.push(new TokenNode(next()));
    }
    
    void shift(int n) throws Exception{
        n=n-stack.size();
        while(n-->0){
            shift();
        }
    }
    
    boolean is(Node n,TokenType tt){
        if(n instanceof TokenNode){
            return ((TokenNode)n).token.is(tt);
        }
        return false;
    }
    
    void match(TokenType tt) throws Exception{
        Node n=stack.pop();
        if(!is(n,tt)){
            throw new Exception("unexpected token "+n);
        }
        //shift();
    }
    
    GrammarToken getToken(int i){
        return ((TokenNode)stack.get(i)).token;
    }
    
    public void parse() throws Exception{
        while(isTokenDecl()){
            tokens.addAll(tokenDecl());
        }
        while(isRuleDecl()){
            rules.add(ruleDecl());
        }
        System.out.println(stack);
    }
    
    boolean isTokenDecl() throws Exception {
        shift();
        return is(stack.peek(),TOKEN);
    }

    List<TokenDecl> tokenDecl() throws Exception {
        match(TOKEN);
        List<TokenDecl> list = new ArrayList<>();
        for (; ; ) {
            //next(1);
            GrammarToken t1 = next();
            if (t1.is(IDENT)) {
                TokenDecl decl = new TokenDecl(t1.value);
                list.add(decl);
                //match(IDENT);
            }
            else if(t1.is(SEMI)){
                break;
            }else{
                throw new Exception("semi expected, got "+t1);
            }

        }
        return list;
    }
    
    
    
    boolean isRuleDecl() throws Exception{
        shift(2);
        GrammarToken t=getToken(0);
        
        if(t.is(IDENT)){
            GrammarToken t2=getToken(1);
            return t2.is(EQ)||t2.is(COLON);
        }
        return false;
    }
    
    RuleDecl ruleDecl() throws Exception{
        shift(2);
        GrammarToken t=getToken(0);
        RuleDecl decl=new RuleDecl(t.value);
        match(IDENT);
        GrammarToken op=getToken(0);
        match(op.type);
        
        return decl;
    }

    @Override
    public String toString()
    {
        for(TokenDecl t:tokens){
            System.out.println(t);
        }
        for(RuleDecl r:rules){
            System.out.println(r);
        }
        return "";
    }
    
    
}
