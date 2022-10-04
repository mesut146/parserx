package mesut.parserx.parser;

import java.util.List;
import java.util.ArrayList;

public class Ast{
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
            sb.append(LEXER_MEMBERS_BEGIN.value);
            first = false;
            if(!LEXER_MEMBER.isEmpty()){
                if(!first){
                    sb.append(", ");
                }
                sb.append('[');
                for(int i = 0;i < LEXER_MEMBER.size();i++){
                    sb.append(LEXER_MEMBER.get(i).value);
                    if(i < LEXER_MEMBER.size() - 1) sb.append(", ");
                }
                sb.append(']');
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            sb.append(MEMBERS_END.value);
            return sb.append("}").toString();
        }
    }
    public static class includeStatement{
        public Token INCLUDE;
        public Token STRING;
        public String toString(){
            StringBuilder sb = new StringBuilder("includeStatement{");
            boolean first = true;
            sb.append(INCLUDE.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(STRING.value);
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
            sb.append(OPTIONS.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(LBRACE.value);
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
            sb.append(RBRACE.value);
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
            sb.append(key.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(EQ.value);
            if(!first){
                sb.append(", ");
            }
            sb.append(value.toString());
            if(SEMI != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(SEMI.value);
                first = false;
            }
            return sb.append("}").toString();
        }
    }
    public static class optiong1{
        public int which;
        Optiong11 NUMBER;
        Optiong12 BOOLEAN;
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
        public static class Optiong11{
            optiong1 holder;
            public Token NUMBER;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(NUMBER.value);
                return sb.toString();
            }
        }
        public static class Optiong12{
            optiong1 holder;
            public Token BOOLEAN;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(BOOLEAN.value);
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
            sb.append(START.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(SEPARATOR.value);
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append(SEMI.value);
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
            sb.append(TOKEN.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(LBRACE.value);
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
            sb.append(RBRACE.value);
            return sb.append("}").toString();
        }
    }
    public static class tokenBlockg1{
        public int which;
        Tokenblockg11 tokenDecl;
        Tokenblockg12 modeBlock;
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
        public static class Tokenblockg11{
            tokenBlockg1 holder;
            public tokenDecl tokenDecl;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(tokenDecl.toString());
                return sb.toString();
            }
        }
        public static class Tokenblockg12{
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
                sb.append(HASH.value);
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
            sb.append(SEPARATOR.value);
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
            sb.append(SEMI.value);
            return sb.append("}").toString();
        }
    }
    public static class tokenDeclg1{
        public Token ARROW;
        public modes modes;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDeclg1{");
            boolean first = true;
            sb.append(ARROW.value);
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
            sb.append(COMMA.value);
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
            sb.append(IDENT.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(LBRACE.value);
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
            sb.append(RBRACE.value);
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
            sb.append(SEPARATOR.value);
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append(SEMI.value);
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
            sb.append(LP.value);
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
            sb.append(RP.value);
            return sb.append("}").toString();
        }
    }
    public static class argsg1{
        public Token COMMA;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("argsg1{");
            boolean first = true;
            sb.append(COMMA.value);
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
            sb.append(OR.value);
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
            sb.append(HASH.value);
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
        Sequenceg11 LEFT;
        Sequenceg12 RIGHT;
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
        public static class Sequenceg11{
            sequenceg1 holder;
            public Token LEFT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(LEFT.value);
                return sb.toString();
            }
        }
        public static class Sequenceg12{
            sequenceg1 holder;
            public Token RIGHT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(RIGHT.value);
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
            sb.append(MINUS.value);
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
        Regex1 regex1;
        Regex2 regex2;
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
        public static class Regex1{
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
                sb.append(EQ.value);
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
                    sb.append(ACTION.value);
                    first = false;
                }
                return sb.toString();
            }
        }
        public static class Regex2{
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
                    sb.append(ACTION.value);
                    first = false;
                }
                return sb.toString();
            }
        }
    }
    public static class regexType{
        public int which;
        Regextype1 STAR;
        Regextype2 PLUS;
        Regextype3 QUES;
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
        public static class Regextype1{
            regexType holder;
            public Token STAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(STAR.value);
                return sb.toString();
            }
        }
        public static class Regextype2{
            regexType holder;
            public Token PLUS;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(PLUS.value);
                return sb.toString();
            }
        }
        public static class Regextype3{
            regexType holder;
            public Token QUES;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(QUES.value);
                return sb.toString();
            }
        }
    }
    public static class simple{
        public int which;
        Simple1 group;
        Simple2 name;
        Simple3 stringNode;
        Simple4 bracketNode;
        Simple5 untilNode;
        Simple6 dotNode;
        Simple7 EPSILON;
        Simple8 SHORTCUT;
        Simple9 call;
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
        public static class Simple1{
            simple holder;
            public group group;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(group.toString());
                return sb.toString();
            }
        }
        public static class Simple2{
            simple holder;
            public name name;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(name.toString());
                return sb.toString();
            }
        }
        public static class Simple3{
            simple holder;
            public stringNode stringNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(stringNode.toString());
                return sb.toString();
            }
        }
        public static class Simple4{
            simple holder;
            public bracketNode bracketNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(bracketNode.toString());
                return sb.toString();
            }
        }
        public static class Simple5{
            simple holder;
            public untilNode untilNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(untilNode.toString());
                return sb.toString();
            }
        }
        public static class Simple6{
            simple holder;
            public dotNode dotNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(dotNode.toString());
                return sb.toString();
            }
        }
        public static class Simple7{
            simple holder;
            public Token EPSILON;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(EPSILON.value);
                return sb.toString();
            }
        }
        public static class Simple8{
            simple holder;
            public Token SHORTCUT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(SHORTCUT.value);
                return sb.toString();
            }
        }
        public static class Simple9{
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
            sb.append(LP.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append(RP.value);
            return sb.append("}").toString();
        }
    }
    public static class stringNode{
        public int which;
        Stringnode1 STRING;
        Stringnode2 CHAR;
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
        public static class Stringnode1{
            stringNode holder;
            public Token STRING;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(STRING.value);
                return sb.toString();
            }
        }
        public static class Stringnode2{
            stringNode holder;
            public Token CHAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(CHAR.value);
                return sb.toString();
            }
        }
    }
    public static class bracketNode{
        public Token BRACKET;
        public String toString(){
            StringBuilder sb = new StringBuilder("bracketNode{");
            boolean first = true;
            sb.append(BRACKET.value);
            return sb.append("}").toString();
        }
    }
    public static class untilNode{
        public Token TILDE;
        public regex regex;
        public String toString(){
            StringBuilder sb = new StringBuilder("untilNode{");
            boolean first = true;
            sb.append(TILDE.value);
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
            sb.append(DOT.value);
            return sb.append("}").toString();
        }
    }
    public static class name{
        public Token IDENT;
        public String toString(){
            StringBuilder sb = new StringBuilder("name{");
            boolean first = true;
            sb.append(IDENT.value);
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
            sb.append(CALL_BEGIN.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(IDENT.value);
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
            sb.append(RP.value);
            return sb.append("}").toString();
        }
    }
    public static class callg1{
        public Token COMMA;
        public Token IDENT;
        public String toString(){
            StringBuilder sb = new StringBuilder("callg1{");
            boolean first = true;
            sb.append(COMMA.value);
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(IDENT.value);
            return sb.append("}").toString();
        }
    }
}
