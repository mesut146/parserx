package grammar2;
import java.io.*;
import nodes.*;
import java.util.*;
import static grammar2.TokenType.*;
import rule.*;

public class GrammarParser
{
    GrammarLexer lexer;
    //GrammarToken t1,t2,t3;
    List<GrammarToken> cache=new ArrayList<>();
    
    public GrammarParser(GrammarLexer lexer){
        this.lexer=lexer;
    }
    
    GrammarToken next() throws IOException{
        GrammarToken tok=lexer.nextToken();
        //cache.add(tok);
        return tok;
    }
    
    void readSingle() throws IOException{
        GrammarToken t=next();
        cache.add(t);
    }
    
    void next(int la) throws IOException{
        int n=la-cache.size();
        while(n-->0){
            cache.add(next());
        }
    }
    
    void nextUntil(TokenType tt) throws IOException{
        GrammarToken t;
        int i=0;
        do{
            t=get(i);
            cache.add(t);
            
        }while(!t.is(tt));
    }
    
    /*void set(){
        if(cache.size()==1){
            t1=cache.get(0);
        }
        else if(cache.size()==2){
            t1=cache.get(0);
            t2=cache.get(2);
        }
    }*/
    
    GrammarToken get(){
        return cache.get(0);
    }
    
    GrammarToken get(int i){
        return cache.get(i);
    }
    
    GrammarToken peek(){
        return cache.get(cache.size()-1);
    }
    
    GrammarToken match(TokenType tt) throws Exception{
        if(get().is(tt)){
            cache.remove(0);
            if(cache.isEmpty()){
                next(1);
            }
        }else{
            throw new Exception("invalid token "+get()+" expected: "+tt);
        }
        return null;
    }
    
    public Tree parse() throws Exception{
        Tree tree=new Tree();
        
        while(isTokenDecl()){
            List<Node> l=(List<Node>)tokenDecl();
            tree.addAll(l);
        }
        while(isRuleDecl()){
            tree.add(ruleDecl());
        }
        return tree;
    }
    
    boolean isTokenDecl() throws IOException{
        next(1);
        if(get().is(TOKEN)){
            return true;
        }
        return false;
    }
    
    List<TokenDecl> tokenDecl() throws Exception{
        match(TOKEN);
        List<TokenDecl> list=new ArrayList<>();
        for(;;){
            //next(1);
            GrammarToken t1=get();
            if(t1.is(IDENT)){
                TokenDecl decl=new TokenDecl(t1.value);
                list.add(decl);
                match(IDENT);
            }else{
                match(SEMI);
                break;
            }

        }
        return list;
    }
    
    boolean isRuleDecl() throws IOException{
        next(2);
        if(get(0).is(IDENT)){
            if(get(1).is(EQ)||get(1).is(COLON)){
                return true;
            }
        }
        return false;
    }
    
    RuleDecl ruleDecl() throws Exception{
        RuleDecl rule=new RuleDecl(get(0).value);
        match(IDENT);
        if(get(0).is(EQ)){
            match(EQ);
        }else{
            match(COLON);
        }
        
        while(true){
            Rule tmp=isRule();
            if(tmp!=null){
                rule.add(tmp);
            }
            
            else{
                match(SEMI);
                break;
            }
            
        }
        return rule;
    }
    
    Rule isRule() throws Exception{
        if(isOptional()){
            return optional();
        }
        else if(isStar()){
            return star();
        }
        else if(isPlus()){
            return plus();
        }
        else if(isGroup()){
            return group();
        }
        else if(isName()){//must be last
            return name();
        }
        return null;
    }
    
    boolean isOptional() throws Exception{
        //group,name
        if(isGroup()){
            readSingle();
            return peek().is(QUES);
        }
        else if(isName()){
            next(2);
            return peek().is(QUES);
        }
        return false;
    }
    
    OptionalRule optional() throws Exception{
        //group or name
       OptionalRule rule=new OptionalRule();
       if(isGroup()){
           rule.rule=group();
       }
       else if(isName()){
           rule.rule=name();
       }
       match(QUES);
       return rule;
    }
    
    boolean isStar() throws Exception{
        //group,name
        if(isGroup()){
            return get(0).is(STAR);
        }
        else if(isName()){
            next(2);
            return peek().is(STAR);
        }
        return false;
    }

    StarRule star() throws Exception{
        //group or name
        StarRule rule=new StarRule();
        if(isGroup()){
            rule.rule=group();
        }
        else if(isName()){
            rule.rule=name();
        }
        match(STAR);
        return rule;
    }
    
    boolean isPlus() throws Exception{
        //group,name
        if(isGroup()){
            return get(0).is(PLUS);
        }
        else if(isName()){
            next(2);
            return peek().is(PLUS);
        }
        return false;
    }

    PlusRule plus() throws Exception{
        //group or name
        PlusRule rule=new PlusRule();
        if(isGroup()){
            rule.rule=group();
        }
        else if(isName()){
            rule.rule=name();
        }
        match(PLUS);
        return rule;
    }
    
    boolean isGroup() throws Exception{
        next(1);
        if(get(0).is(LPAREN)){
            //fix use cache
            nextUntil(RPAREN);
            return true;
        }
        return false;
    }
    
    GroupRule group() throws Exception{
        next(1);
        match(LPAREN);
        GroupRule rule=new GroupRule();
        //any rule*
        return rule;
    }
    
    boolean isName() throws IOException{
        next(1);
        return get(0).is(IDENT);
    }
    
    RuleRef name() throws Exception{
        next(1);
        RuleRef rule=new RuleRef(get(0).value);
        match(IDENT);
        return rule;
    }
    OrNode orNode() throws IOException{
        OrNode node=new OrNode();
        GrammarToken token=next();
        return node;
    }
}
