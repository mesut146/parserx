package grammar2;

import java.util.*;
import nodes.*;
import rule.*;
import java.io.*;
import static grammar2.TokenType.*;

public class GrammarParser2
{
    public Tree tree;
    List<GrammarToken> stack=new ArrayList<>();
    GrammarLexer lexer;
    
    public GrammarParser2(GrammarLexer lexer){
        this.lexer=lexer;
        tree=new Tree();
    }
    
    GrammarToken next() throws IOException {
        return lexer.nextToken();
    }
    
    
    void shift() throws Exception{
        stack.add(next());
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
    
    boolean is(TokenType...arr){
        int i=0;
        for(TokenType tt:arr){
            if(!getToken(i++).is(tt)){
                return false;
            }
        }
        return true;
    }
    
    GrammarToken first(){
        return stack.remove(0);
    }
    
    GrammarToken match(TokenType tt) throws Exception{
        //java.util.
        
        GrammarToken n=first();
        if(!n.is(tt)){
            throw new Exception("unexpected token "+n);
        }
        return n;
        //shift();
    }
    
    GrammarToken getToken(int i){
        return stack.get(i);
    }
    
    public void parse() throws Exception{
        if(isTokenBlock()){
            tokenBlock();
        }
        
        System.out.println(stack);
    }
    
    boolean isTokenBlock() throws Exception{
        shift(2);
        return is(TOKENS,LBRACE);
    }
    
    void tokenBlock() throws Exception{
        match(TOKENS);
        match(LBRACE);
        
        while(isTokenDecl()){
            tree.addToken(tokenDecl());
        }
        
        match(RBRACE);
    }
    
    boolean isTokenDecl() throws Exception {
        shift(2);
        return is(IDENT,EQ);
    }

    TokenDecl tokenDecl() throws Exception {
        shift();
        
        GrammarToken t1 = match(IDENT);
        match(EQ);
        TokenDecl decl = new TokenDecl(t1.value);
        rhs(decl);
       
        return decl;
    }
    
    void rhs(TokenDecl decl) throws Exception{
        if(isBracket()){
            bracket();
        }
    }
    
    boolean isBracket() throws Exception{
        shift();
        return getToken(0).is(LBRACKET);
    }
    
    void bracket() throws Exception{
        match(LBRACKET);
        //char or char range
       // GrammarToken t=match();
        
        match(RBRACKET);
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
        return "";
    }
    
    
}
