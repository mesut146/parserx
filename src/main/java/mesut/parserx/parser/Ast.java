package mesut.parserx.parser;

import java.util.List;
import java.util.ArrayList;

public class Ast{

    static void printToken(Token token, StringBuilder sb){
        sb.append("'").append(token.formatValue()).append("'");
    }

    public static class tree{
        public List<includeStatement> includeStatement = new ArrayList<>();
        public optionsBlock optionsBlock;
        public lexerMembers lexerMembers;
        public List<tokenBlock> tokens = new ArrayList<>();
        public startDecl startDecl;
        public List<ruleDecl> rules = new ArrayList<>();
        public String toString(){
            StringBuilder sb = new StringBuilder("tree{");
            boolean first = true;
            if(!includeStatement.isEmpty()){
                sb.append('[');
                for(int i = 0;i < includeStatement.size();i++){
                    sb.append(includeStatement.get(i).toString());
                    if(i < includeStatement.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(optionsBlock != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(optionsBlock.toString());
                first = false;
            }
            if(lexerMembers != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(lexerMembers.toString());
                first = false;
            }
            if(!tokens.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < tokens.size();i++){
                    sb.append(tokens.get(i).toString());
                    if(i < tokens.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(startDecl != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(startDecl.toString());
                first = false;
            }
            if(!rules.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < rules.size();i++){
                    sb.append(rules.get(i).toString());
                    if(i < rules.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class lexerMembers{
        public Token LEXER_MEMBERS_BEGIN;
        public List<Token> LEXER_MEMBER = new ArrayList<>();
        public Token MEMBERS_END;
        public String toString(){
            StringBuilder sb = new StringBuilder("lexerMembers{");
            boolean first = true;
            printToken(LEXER_MEMBERS_BEGIN, sb);
            first = false;
            if(!LEXER_MEMBER.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < LEXER_MEMBER.size();i++){
                    printToken(LEXER_MEMBER.get(i), sb);
                    if(i < LEXER_MEMBER.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(MEMBERS_END, sb);
            return sb.append("}").toString();
        }
    }
    public static class includeStatement{
        public Token INCLUDE;
        public Token STRING;
        public String toString(){
            StringBuilder sb = new StringBuilder("includeStatement{");
            boolean first = true;
            printToken(INCLUDE, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(STRING, sb);
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
            boolean first = true;
            printToken(OPTIONS, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(LBRACE, sb);
            if(!option.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < option.size();i++){
                    sb.append(option.get(i).toString());
                    if(i < option.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(RBRACE, sb);
            return sb.append("}").toString();
        }
    }
    public static class option{
        public Token key;
        public Token EQ;
        public optiong1 value;
        public Token SEMI;
        public String toString(){
            StringBuilder sb = new StringBuilder("option{");
            boolean first = true;
            printToken(key, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(EQ, sb);
            if(!first){
                sb.append(", ");
            }
            sb.append(value.toString());
            if(SEMI != null){
                if(!first){
                    sb.append(", ");
                }
                printToken(SEMI, sb);
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class optiong1{
        public int which;
        Alt1 NUMBER;
        Alt2 BOOLEAN;
        public String toString(){
            StringBuilder sb = new StringBuilder("optiong1#" + which + "{");
            if(which == 1){
                sb.append(NUMBER);
            }
            else if(which == 2){
                sb.append(BOOLEAN);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            optiong1 holder;
            public Token NUMBER;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(NUMBER, sb);
                return sb.toString();
            }
        }
        public static class Alt2{
            optiong1 holder;
            public Token BOOLEAN;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(BOOLEAN, sb);
                return sb.toString();
            }
        }
    }
    public static class startDecl{
        public Token START;
        public Token SEPARATOR;
        public name name;
        public Token SEMI;
        public String toString(){
            StringBuilder sb = new StringBuilder("startDecl{");
            boolean first = true;
            printToken(START, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(SEPARATOR, sb);
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            if(!first){
                sb.append(", ");
            }
            printToken(SEMI, sb);
            return sb.append("}").toString();
        }
    }
    public static class tokenBlock{
        public Token TOKEN;
        public Token LBRACE;
        public List<tokenBlockg1> g1 = new ArrayList<>();
        public Token RBRACE;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenBlock{");
            boolean first = true;
            printToken(TOKEN, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(LBRACE, sb);
            if(!g1.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(RBRACE, sb);
            return sb.append("}").toString();
        }
    }
    public static class tokenBlockg1{
        public int which;
        Alt1 tokenDecl;
        Alt2 modeBlock;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenBlockg1#" + which + "{");
            if(which == 1){
                sb.append(tokenDecl);
            }
            else if(which == 2){
                sb.append(modeBlock);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            tokenBlockg1 holder;
            public tokenDecl tokenDecl;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(tokenDecl.toString());
                return sb.toString();
            }
        }
        public static class Alt2{
            tokenBlockg1 holder;
            public modeBlock modeBlock;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(modeBlock.toString());
                return sb.toString();
            }
        }
    }
    public static class tokenDecl{
        public Token HASH;
        public name name;
        public Token SEPARATOR;
        public rhs rhs;
        public tokenDeclg1 mode;
        public Token SEMI;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDecl{");
            boolean first = true;
            if(HASH != null){
                printToken(HASH, sb);
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(SEPARATOR, sb);
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(mode != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(mode.toString());
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(SEMI, sb);
            return sb.append("}").toString();
        }
    }
    public static class tokenDeclg1{
        public Token ARROW;
        public modes modes;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDeclg1{");
            boolean first = true;
            printToken(ARROW, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(modes.toString());
            return sb.append("}").toString();
        }
    }
    public static class modes{
        public name name;
        public modesg1 g1;
        public String toString(){
            StringBuilder sb = new StringBuilder("modes{");
            boolean first = true;
            sb.append(name.toString());
            first = false;
            if(g1 != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(g1.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class modesg1{
        public Token COMMA;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("modesg1{");
            boolean first = true;
            printToken(COMMA, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class modeBlock{
        public Token IDENT;
        public Token LBRACE;
        public List<tokenDecl> tokenDecl = new ArrayList<>();
        public Token RBRACE;
        public String toString(){
            StringBuilder sb = new StringBuilder("modeBlock{");
            boolean first = true;
            printToken(IDENT, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(LBRACE, sb);
            if(!tokenDecl.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < tokenDecl.size();i++){
                    sb.append(tokenDecl.get(i).toString());
                    if(i < tokenDecl.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(RBRACE, sb);
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
            boolean first = true;
            sb.append(name.toString());
            first = false;
            if(args != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(args.toString());
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(SEPARATOR, sb);
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            printToken(SEMI, sb);
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
            boolean first = true;
            printToken(LP, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            if(!rest.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < rest.size();i++){
                    sb.append(rest.get(i).toString());
                    if(i < rest.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(RP, sb);
            return sb.append("}").toString();
        }
    }
    public static class argsg1{
        public Token COMMA;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("argsg1{");
            boolean first = true;
            printToken(COMMA, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class rhs{
        public sequence sequence;
        public List<rhsg1> g1 = new ArrayList<>();
        public String toString(){
            StringBuilder sb = new StringBuilder("rhs{");
            boolean first = true;
            sb.append(sequence.toString());
            first = false;
            if(!g1.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class rhsg1{
        public Token OR;
        public sequence sequence;
        public String toString(){
            StringBuilder sb = new StringBuilder("rhsg1{");
            boolean first = true;
            printToken(OR, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(sequence.toString());
            return sb.append("}").toString();
        }
    }
    public static class sequence{
        public List<sub> sub = new ArrayList<>();
        public sequenceg1 assoc;
        public sequenceg2 label;
        public String toString(){
            StringBuilder sb = new StringBuilder("sequence{");
            boolean first = true;
            if(!sub.isEmpty()){
                sb.append('[');
                for(int i = 0;i < sub.size();i++){
                    sb.append(sub.get(i).toString());
                    if(i < sub.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(assoc != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(assoc.toString());
                first = false;
            }
            if(label != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(label.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class sequenceg2{
        public Token HASH;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("sequenceg2{");
            boolean first = true;
            printToken(HASH, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            return sb.append("}").toString();
        }
    }
    public static class sequenceg1{
        public int which;
        Alt1 LEFT;
        Alt2 RIGHT;
        public String toString(){
            StringBuilder sb = new StringBuilder("sequenceg1#" + which + "{");
            if(which == 1){
                sb.append(LEFT);
            }
            else if(which == 2){
                sb.append(RIGHT);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            sequenceg1 holder;
            public Token LEFT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(LEFT, sb);
                return sb.toString();
            }
        }
        public static class Alt2{
            sequenceg1 holder;
            public Token RIGHT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(RIGHT, sb);
                return sb.toString();
            }
        }
    }
    public static class sub{
        public regex regex;
        public subg1 g1;
        public String toString(){
            StringBuilder sb = new StringBuilder("sub{");
            boolean first = true;
            sb.append(regex.toString());
            first = false;
            if(g1 != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(g1.toString());
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class subg1{
        public Token MINUS;
        public stringNode stringNode;
        public String toString(){
            StringBuilder sb = new StringBuilder("subg1{");
            boolean first = true;
            printToken(MINUS, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(stringNode.toString());
            return sb.append("}").toString();
        }
    }
    public static class regex{
        public int which;
        Alt1 alt1;
        Alt2 alt2;
        public String toString(){
            StringBuilder sb = new StringBuilder("regex#" + which + "{");
            if(which == 1){
                sb.append(alt1);
            }
            else if(which == 2){
                sb.append(alt2);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            regex holder;
            public name name;
            public Token EQ;
            public simple simple;
            public regexType type;
            public Token ACTION;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(name.toString());
                first = false;
                if(!first){
                    sb.append(", ");
                }
                printToken(EQ, sb);
                if(!first){
                    sb.append(", ");
                }
                sb.append(simple.toString());
                if(type != null){
                    if(!first){
                        sb.append(", ");
                    }
                    sb.append(type.toString());
                    first = false;
                }
                if(ACTION != null){
                    if(!first){
                        sb.append(", ");
                    }
                    printToken(ACTION, sb);
                    first = false;
                }
                return sb.toString();
            }
        }
        public static class Alt2{
            regex holder;
            public simple simple;
            public regexType type;
            public Token ACTION;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(simple.toString());
                first = false;
                if(type != null){
                    if(!first){
                        sb.append(", ");
                    }
                    sb.append(type.toString());
                    first = false;
                }
                if(ACTION != null){
                    if(!first){
                        sb.append(", ");
                    }
                    printToken(ACTION, sb);
                    first = false;
                }
                return sb.toString();
            }
        }
    }
    public static class regexType{
        public int which;
        Alt1 STAR;
        Alt2 PLUS;
        Alt3 QUES;
        public String toString(){
            StringBuilder sb = new StringBuilder("regexType#" + which + "{");
            if(which == 1){
                sb.append(STAR);
            }
            else if(which == 2){
                sb.append(PLUS);
            }
            else if(which == 3){
                sb.append(QUES);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            regexType holder;
            public Token STAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(STAR, sb);
                return sb.toString();
            }
        }
        public static class Alt2{
            regexType holder;
            public Token PLUS;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(PLUS, sb);
                return sb.toString();
            }
        }
        public static class Alt3{
            regexType holder;
            public Token QUES;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(QUES, sb);
                return sb.toString();
            }
        }
    }
    public static class simple{
        public int which;
        Alt1 group;
        Alt2 name;
        Alt3 stringNode;
        Alt4 bracketNode;
        Alt5 untilNode;
        Alt6 dotNode;
        Alt7 EPSILON;
        Alt8 SHORTCUT;
        Alt9 call;
        public String toString(){
            StringBuilder sb = new StringBuilder("simple#" + which + "{");
            if(which == 1){
                sb.append(group);
            }
            else if(which == 2){
                sb.append(name);
            }
            else if(which == 3){
                sb.append(stringNode);
            }
            else if(which == 4){
                sb.append(bracketNode);
            }
            else if(which == 5){
                sb.append(untilNode);
            }
            else if(which == 6){
                sb.append(dotNode);
            }
            else if(which == 7){
                sb.append(EPSILON);
            }
            else if(which == 8){
                sb.append(SHORTCUT);
            }
            else if(which == 9){
                sb.append(call);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            simple holder;
            public group group;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(group.toString());
                return sb.toString();
            }
        }
        public static class Alt2{
            simple holder;
            public name name;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(name.toString());
                return sb.toString();
            }
        }
        public static class Alt3{
            simple holder;
            public stringNode stringNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(stringNode.toString());
                return sb.toString();
            }
        }
        public static class Alt4{
            simple holder;
            public bracketNode bracketNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(bracketNode.toString());
                return sb.toString();
            }
        }
        public static class Alt5{
            simple holder;
            public untilNode untilNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(untilNode.toString());
                return sb.toString();
            }
        }
        public static class Alt6{
            simple holder;
            public dotNode dotNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(dotNode.toString());
                return sb.toString();
            }
        }
        public static class Alt7{
            simple holder;
            public Token EPSILON;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(EPSILON, sb);
                return sb.toString();
            }
        }
        public static class Alt8{
            simple holder;
            public Token SHORTCUT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(SHORTCUT, sb);
                return sb.toString();
            }
        }
        public static class Alt9{
            simple holder;
            public call call;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(call.toString());
                return sb.toString();
            }
        }
    }
    public static class group{
        public Token LP;
        public rhs rhs;
        public Token RP;
        public String toString(){
            StringBuilder sb = new StringBuilder("group{");
            boolean first = true;
            printToken(LP, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            printToken(RP, sb);
            return sb.append("}").toString();
        }
    }
    public static class stringNode{
        public int which;
        Alt1 STRING;
        Alt2 CHAR;
        public String toString(){
            StringBuilder sb = new StringBuilder("stringNode#" + which + "{");
            if(which == 1){
                sb.append(STRING);
            }
            else if(which == 2){
                sb.append(CHAR);
            }
            return sb.append("}").toString();
        }
        public static class Alt1{
            stringNode holder;
            public Token STRING;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(STRING, sb);
                return sb.toString();
            }
        }
        public static class Alt2{
            stringNode holder;
            public Token CHAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                printToken(CHAR, sb);
                return sb.toString();
            }
        }
    }
    public static class bracketNode{
        public Token BRACKET;
        public String toString(){
            StringBuilder sb = new StringBuilder("bracketNode{");
            boolean first = true;
            printToken(BRACKET, sb);
            return sb.append("}").toString();
        }
    }
    public static class untilNode{
        public Token TILDE;
        public regex regex;
        public String toString(){
            StringBuilder sb = new StringBuilder("untilNode{");
            boolean first = true;
            printToken(TILDE, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(regex.toString());
            return sb.append("}").toString();
        }
    }
    public static class dotNode{
        public Token DOT;
        public String toString(){
            StringBuilder sb = new StringBuilder("dotNode{");
            boolean first = true;
            printToken(DOT, sb);
            return sb.append("}").toString();
        }
    }
    public static class name{
        public Token IDENT;
        public String toString(){
            StringBuilder sb = new StringBuilder("name{");
            boolean first = true;
            printToken(IDENT, sb);
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
            boolean first = true;
            printToken(CALL_BEGIN, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(IDENT, sb);
            if(!g1.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < g1.size();i++){
                    sb.append(g1.get(i).toString());
                    if(i < g1.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            printToken(RP, sb);
            return sb.append("}").toString();
        }
    }
    public static class callg1{
        public Token COMMA;
        public Token IDENT;
        public String toString(){
            StringBuilder sb = new StringBuilder("callg1{");
            boolean first = true;
            printToken(COMMA, sb);
            first = false;
            if(!first){
                sb.append(", ");
            }
            printToken(IDENT, sb);
            return sb.append("}").toString();
        }
    }
}
