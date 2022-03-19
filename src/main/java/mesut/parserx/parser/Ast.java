package mesut.parserx.parser;

import java.util.List;
import java.util.ArrayList;

public class Ast{
    public static class tree{
        public List<includeStatement> includeStatement = new ArrayList<>();
        public optionsBlock optionsBlock;
        public List<treeg1> tokens = new ArrayList<>();
        public startDecl startDecl;
        public List<ruleDecl> rules = new ArrayList<>();

        public String toString(){
            StringBuilder sb = new StringBuilder("tree{");
            if(!includeStatement.isEmpty()){
                sb.append('[');
                for(int i = 0;i < includeStatement.size();i++){
                    sb.append(includeStatement.get(i).toString());
                    if(i < includeStatement.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            if(optionsBlock != null) sb.append(",");
            sb.append(optionsBlock == null?"":optionsBlock.toString());
            if(!tokens.isEmpty()) sb.append(",");
            if(!tokens.isEmpty()){
                sb.append('[');
                for(int i = 0;i < tokens.size();i++){
                    sb.append(tokens.get(i).toString());
                    if(i < tokens.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            if(startDecl != null) sb.append(",");
            sb.append(startDecl == null?"":startDecl.toString());
            if(!rules.isEmpty()) sb.append(",");
            if(!rules.isEmpty()){
                sb.append('[');
                for(int i = 0;i < rules.size();i++){
                    sb.append(rules.get(i).toString());
                    if(i < rules.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            return sb.append("}").toString();
        }
    }
    public static class treeg1{
        public int which;
        public tokenBlock tokenBlock;
        public skipBlock skipBlock;

        public String toString(){
            StringBuilder sb = new StringBuilder("treeg1#" + which + "{");
            if(which == 1){
                sb.append(tokenBlock.toString());
            }
            else if(which == 2){
                sb.append(skipBlock.toString());
            }
            return sb.append("}").toString();
        }
    }
    public static class includeStatement{
        public Token INCLUDE;
        public Token STRING;

        public String toString(){
            StringBuilder sb = new StringBuilder("includeStatement{");
            sb.append("'" + INCLUDE.value + "'");
            sb.append(",");
            sb.append("'" + STRING.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class optionsBlock{
        public Token OPTIONS;
        public Token LBRACE;
        public List<option> option = new ArrayList<>();
        public Token RBRACE;

        public String toString(){
            StringBuilder sb = new StringBuilder("optionsBlock{");
            sb.append("'" + OPTIONS.value + "'");
            sb.append(",");
            sb.append("'" + LBRACE.value + "'");
            if(!option.isEmpty()) sb.append(",");
            if(!option.isEmpty()){
                sb.append('[');
                for(int i = 0;i < option.size();i++){
                    sb.append(option.get(i).toString());
                    if(i < option.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + RBRACE.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class option{
        public Token key;
        public Token SEPARATOR;
        public optiong1 value;
        public Token SEMI;

        public String toString(){
            StringBuilder sb = new StringBuilder("option{");
            sb.append("'" + key.value + "'");
            sb.append(",");
            sb.append("'" + SEPARATOR.value + "'");
            sb.append(",");
            sb.append(value.toString());
            if(SEMI != null) sb.append(",");
            sb.append(SEMI == null?"":"'" + SEMI.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class optiong1{
        public int which;
        public Token NUMBER;
        public Token BOOLEAN;

        public String toString(){
            StringBuilder sb = new StringBuilder("optiong1#" + which + "{");
            if(which == 1){
                sb.append("'" + NUMBER.value + "'");
            }
            else if(which == 2){
                sb.append("'" + BOOLEAN.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class startDecl{
        public Token START;
        public Token SEPARATOR;
        public name name;
        public Token SEMI;

        public String toString(){
            StringBuilder sb = new StringBuilder("startDecl{");
            sb.append("'" + START.value + "'");
            sb.append(",");
            sb.append("'" + SEPARATOR.value + "'");
            sb.append(",");
            sb.append(name.toString());
            sb.append(",");
            sb.append("'" + SEMI.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class tokenBlock{
        public Token TOKEN;
        public Token LBRACE;
        public List<tokenDecl> tokenDecl = new ArrayList<>();
        public Token RBRACE;

        public String toString(){
            StringBuilder sb = new StringBuilder("tokenBlock{");
            sb.append("'" + TOKEN.value + "'");
            sb.append(",");
            sb.append("'" + LBRACE.value + "'");
            if(!tokenDecl.isEmpty()) sb.append(",");
            if(!tokenDecl.isEmpty()){
                sb.append('[');
                for(int i = 0;i < tokenDecl.size();i++){
                    sb.append(tokenDecl.get(i).toString());
                    if(i < tokenDecl.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + RBRACE.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class skipBlock{
        public Token SKIP;
        public Token LBRACE;
        public List<tokenDecl> tokenDecl = new ArrayList<>();
        public Token RBRACE;

        public String toString(){
            StringBuilder sb = new StringBuilder("skipBlock{");
            sb.append("'" + SKIP.value + "'");
            sb.append(",");
            sb.append("'" + LBRACE.value + "'");
            if(!tokenDecl.isEmpty()) sb.append(",");
            if(!tokenDecl.isEmpty()){
                sb.append('[');
                for(int i = 0;i < tokenDecl.size();i++){
                    sb.append(tokenDecl.get(i).toString());
                    if(i < tokenDecl.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + RBRACE.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class tokenDecl{
        public Token HASH;
        public name name;
        public tokenDeclg1 g1;
        public Token SEPARATOR;
        public rhs rhs;
        public Token SEMI;

        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDecl{");
            sb.append(HASH == null?"":"'" + HASH.value + "'");
            sb.append(",");
            sb.append(name.toString());
            if(g1 != null) sb.append(",");
            sb.append(g1 == null?"":g1.toString());
            sb.append(",");
            sb.append("'" + SEPARATOR.value + "'");
            sb.append(",");
            sb.append(rhs.toString());
            sb.append(",");
            sb.append("'" + SEMI.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class tokenDeclg1{
        public Token MINUS;
        public name name;

        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDeclg1{");
            sb.append("'" + MINUS.value + "'");
            sb.append(",");
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class ruleDecl{
        public name name;
        public args args;
        public Token SEPARATOR;
        public rhs rhs;
        public Token SEMI;

        public String toString(){
            StringBuilder sb = new StringBuilder("ruleDecl{");
            sb.append(name.toString());
            if(args != null) sb.append(",");
            sb.append(args == null?"":args.toString());
            sb.append(",");
            sb.append("'" + SEPARATOR.value + "'");
            sb.append(",");
            sb.append(rhs.toString());
            sb.append(",");
            sb.append("'" + SEMI.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class args{
        public Token LP;
        public name name;
        public List<argsg1> rest = new ArrayList<>();
        public Token RP;

        public String toString(){
            StringBuilder sb = new StringBuilder("args{");
            sb.append("'" + LP.value + "'");
            sb.append(",");
            sb.append(name.toString());
            if(!rest.isEmpty()) sb.append(",");
            if(!rest.isEmpty()){
                sb.append('[');
                for(int i = 0;i < rest.size();i++){
                    sb.append(rest.get(i).toString());
                    if(i < rest.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + RP.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class argsg1{
        public Token COMMA;
        public name name;

        public String toString(){
            StringBuilder sb = new StringBuilder("argsg1{");
            sb.append("'" + COMMA.value + "'");
            sb.append(",");
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class rhs{
        public sequence sequence;
        public List<rhsg1> g1 = new ArrayList<>();

        public String toString(){
            StringBuilder sb = new StringBuilder("rhs{");
            sb.append(sequence.toString());
            if(!g1.isEmpty()) sb.append(",");
            if(!g1.isEmpty()){
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            return sb.append("}").toString();
        }
    }
    public static class rhsg1{
        public Token OR;
        public sequence sequence;

        public String toString(){
            StringBuilder sb = new StringBuilder("rhsg1{");
            sb.append("'" + OR.value + "'");
            sb.append(",");
            sb.append(sequence.toString());
            return sb.append("}").toString();
        }
    }
    public static class sequence{
        public List<regex> regex = new ArrayList<>();
        public sequenceg1 assoc;
        public sequenceg2 label;

        public String toString(){
            StringBuilder sb = new StringBuilder("sequence{");
            if(!regex.isEmpty()){
                sb.append('[');
                for(int i = 0;i < regex.size();i++){
                    sb.append(regex.get(i).toString());
                    if(i < regex.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            if(assoc != null) sb.append(",");
            sb.append(assoc == null?"":assoc.toString());
            if(label != null) sb.append(",");
            sb.append(label == null?"":label.toString());
            return sb.append("}").toString();
        }
    }
    public static class sequenceg2{
        public Token HASH;
        public name name;

        public String toString(){
            StringBuilder sb = new StringBuilder("sequenceg2{");
            sb.append("'" + HASH.value + "'");
            sb.append(",");
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class sequenceg1{
        public int which;
        public Token LEFT;
        public Token RIGHT;

        public String toString(){
            StringBuilder sb = new StringBuilder("sequenceg1#" + which + "{");
            if(which == 1){
                sb.append("'" + LEFT.value + "'");
            }
            else if(which == 2){
                sb.append("'" + RIGHT.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class regex{
        public int which;
        Regex1 regex1;
        Regex2 regex2;
        public static class Regex1{
                public name name;
                public Token SEPARATOR;
                public simple simple;
                public regexg1 type;
                public String toString(){
                        StringBuilder sb = new StringBuilder();
                        sb.append(name.toString());
                        sb.append(",");
                        sb.append("'" + SEPARATOR.value + "'");
                        sb.append(",");
                        sb.append(simple.toString());
                        if(type != null) sb.append(",");
                        sb.append(type == null?"":type.toString());
                        return sb.toString();
                }
        }
        public static class Regex2{
                public simple simple;
                public regexg2 type;
                public String toString(){
                        StringBuilder sb = new StringBuilder();
                        sb.append(simple.toString());
                        if(type != null) sb.append(",");
                        sb.append(type == null?"":type.toString());
                        return sb.toString();
                }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("regex#" + which + "{");
            if(which == 1){
                sb.append(regex1);
            }
            else if(which == 2){
                sb.append(regex2);
            }
            return sb.append("}").toString();
        }
    }
    public static class regexg2{
        public int which;
        public Token STAR;
        public Token PLUS;
        public Token QUES;

        public String toString(){
            StringBuilder sb = new StringBuilder("regexg2#" + which + "{");
            if(which == 1){
                sb.append("'" + STAR.value + "'");
            }
            else if(which == 2){
                sb.append("'" + PLUS.value + "'");
            }
            else if(which == 3){
                sb.append("'" + QUES.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class regexg1{
        public int which;
        public Token STAR;
        public Token PLUS;
        public Token QUES;

        public String toString(){
            StringBuilder sb = new StringBuilder("regexg1#" + which + "{");
            if(which == 1){
                sb.append("'" + STAR.value + "'");
            }
            else if(which == 2){
                sb.append("'" + PLUS.value + "'");
            }
            else if(which == 3){
                sb.append("'" + QUES.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class simple{
        public int which;
        public group group;
        public name name;
        public stringNode stringNode;
        public bracketNode bracketNode;
        public untilNode untilNode;
        public dotNode dotNode;
        public Token EPSILON;
        public repeatNode repeatNode;
        public Token SHORTCUT;
        public call call;

        public String toString(){
            StringBuilder sb = new StringBuilder("simple#" + which + "{");
            if(which == 1){
                sb.append(group.toString());
            }
            else if(which == 2){
                sb.append(name.toString());
            }
            else if(which == 3){
                sb.append(stringNode.toString());
            }
            else if(which == 4){
                sb.append(bracketNode.toString());
            }
            else if(which == 5){
                sb.append(untilNode.toString());
            }
            else if(which == 6){
                sb.append(dotNode.toString());
            }
            else if(which == 7){
                sb.append("'" + EPSILON.value + "'");
            }
            else if(which == 8){
                sb.append(repeatNode.toString());
            }
            else if(which == 9){
                sb.append("'" + SHORTCUT.value + "'");
            }
            else if(which == 10){
                sb.append(call.toString());
            }
            return sb.append("}").toString();
        }
    }
    public static class group{
        public Token LP;
        public rhs rhs;
        public Token RP;

        public String toString(){
            StringBuilder sb = new StringBuilder("group{");
            sb.append("'" + LP.value + "'");
            sb.append(",");
            sb.append(rhs.toString());
            sb.append(",");
            sb.append("'" + RP.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class stringNode{
        public int which;
        public Token STRING;
        public Token CHAR;

        public String toString(){
            StringBuilder sb = new StringBuilder("stringNode#" + which + "{");
            if(which == 1){
                sb.append("'" + STRING.value + "'");
            }
            else if(which == 2){
                sb.append("'" + CHAR.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class bracketNode{
        public Token BRACKET;

        public String toString(){
            StringBuilder sb = new StringBuilder("bracketNode{");
            sb.append("'" + BRACKET.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class untilNode{
        public Token TILDE;
        public regex regex;

        public String toString(){
            StringBuilder sb = new StringBuilder("untilNode{");
            sb.append("'" + TILDE.value + "'");
            sb.append(",");
            sb.append(regex.toString());
            return sb.append("}").toString();
        }
    }
    public static class dotNode{
        public Token DOT;

        public String toString(){
            StringBuilder sb = new StringBuilder("dotNode{");
            sb.append("'" + DOT.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class name{
        public int which;
        public Token IDENT;
        public Token TOKEN;
        public Token SKIP;
        public Token OPTIONS;
        public Token INCLUDE;

        public String toString(){
            StringBuilder sb = new StringBuilder("name#" + which + "{");
            if(which == 1){
                sb.append("'" + IDENT.value + "'");
            }
            else if(which == 2){
                sb.append("'" + TOKEN.value + "'");
            }
            else if(which == 3){
                sb.append("'" + SKIP.value + "'");
            }
            else if(which == 4){
                sb.append("'" + OPTIONS.value + "'");
            }
            else if(which == 5){
                sb.append("'" + INCLUDE.value + "'");
            }
            return sb.append("}").toString();
        }
    }
    public static class repeatNode{
        public Token LBRACE;
        public rhs rhs;
        public Token RBRACE;

        public String toString(){
            StringBuilder sb = new StringBuilder("repeatNode{");
            sb.append("'" + LBRACE.value + "'");
            sb.append(",");
            sb.append(rhs.toString());
            sb.append(",");
            sb.append("'" + RBRACE.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class call{
        public Token CALL_BEGIN;
        public Token IDENT;
        public List<callg1> g1 = new ArrayList<>();
        public Token RP;

        public String toString(){
            StringBuilder sb = new StringBuilder("call{");
            sb.append("'" + CALL_BEGIN.value + "'");
            sb.append(",");
            sb.append("'" + IDENT.value + "'");
            if(!g1.isEmpty()) sb.append(",");
            if(!g1.isEmpty()){
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(",");
                }
                sb.append(']');
            }
            sb.append(",");
            sb.append("'" + RP.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class callg1{
        public Token COMMA;
        public Token IDENT;

        public String toString(){
            StringBuilder sb = new StringBuilder("callg1{");
            sb.append("'" + COMMA.value + "'");
            sb.append(",");
            sb.append("'" + IDENT.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class join{
        public Token JOIN;
        public Token LP;
        public Token COMMA;
        public Token RP;

        public String toString(){
            StringBuilder sb = new StringBuilder("join{");
            sb.append("'" + JOIN.value + "'");
            sb.append(",");
            sb.append("'" + LP.value + "'");
            sb.append(",");
            sb.append("'" + COMMA.value + "'");
            sb.append(",");
            sb.append("'" + RP.value + "'");
            return sb.append("}").toString();
        }
    }
    public static class nameOrString{
        public int which;
        public name name;
        public stringNode stringNode;

        public String toString(){
            StringBuilder sb = new StringBuilder("nameOrString#" + which + "{");
            if(which == 1){
                sb.append(name.toString());
            }
            else if(which == 2){
                sb.append(stringNode.toString());
            }
            return sb.append("}").toString();
        }
    }
}
