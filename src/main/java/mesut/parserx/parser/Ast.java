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
    public static class treeg1{
        public int which;
        Treeg11 tokenBlock;
        Treeg12 skipBlock;
        public String toString(){
            StringBuilder sb = new StringBuilder("treeg1#" + which + "{");
            if(which == 1){
                sb.append(tokenBlock);
            }
            else if(which == 2){
                sb.append(skipBlock);
            }
            return sb.append("}").toString();
        }
        public static class Treeg11{
            treeg1 holder;
            public tokenBlock tokenBlock;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(tokenBlock.toString());
                return sb.toString();
            }
        }
        public static class Treeg12{
            treeg1 holder;
            public skipBlock skipBlock;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(skipBlock.toString());
                return sb.toString();
            }
        }
    }
    public static class includeStatement{
        public Token INCLUDE;
        public Token STRING;
        public String toString(){
            StringBuilder sb = new StringBuilder("includeStatement{");
            boolean first = true;
            sb.append("'").append(INCLUDE.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(STRING.value).append("'");
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
            sb.append("'").append(OPTIONS.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(LBRACE.value).append("'");
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
            sb.append("'").append(RBRACE.value).append("'");
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
            boolean first = true;
            sb.append("'").append(key.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEPARATOR.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append(value.toString());
            if(SEMI != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append("'").append(SEMI.value).append("'");
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
                sb.append("'").append(NUMBER.value).append("'");
                return sb.toString();
            }
        }
        public static class Optiong12{
            optiong1 holder;
            public Token BOOLEAN;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(BOOLEAN.value).append("'");
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
            sb.append("'").append(START.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEPARATOR.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEMI.value).append("'");
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
            boolean first = true;
            sb.append("'").append(TOKEN.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(LBRACE.value).append("'");
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
            sb.append("'").append(RBRACE.value).append("'");
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
            boolean first = true;
            sb.append("'").append(SKIP.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(LBRACE.value).append("'");
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
            sb.append("'").append(RBRACE.value).append("'");
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
            boolean first = true;
            if(HASH != null){
                sb.append("'").append(HASH.value).append("'");
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            sb.append(name.toString());
            first = false;
            if(g1 != null){
                if(!first){
                    sb.append(", ");
                }
                sb.append(g1.toString());
                first = false;
            }
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEPARATOR.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEMI.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class tokenDeclg1{
        public Token MINUS;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("tokenDeclg1{");
            boolean first = true;
            sb.append("'").append(MINUS.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
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
            sb.append("'").append(SEPARATOR.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(SEMI.value).append("'");
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
            sb.append("'").append(LP.value).append("'");
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
            sb.append("'").append(RP.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class argsg1{
        public Token COMMA;
        public name name;
        public String toString(){
            StringBuilder sb = new StringBuilder("argsg1{");
            boolean first = true;
            sb.append("'").append(COMMA.value).append("'");
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
            sb.append("'").append(OR.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
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
            boolean first = true;
            if(!regex.isEmpty()){
                sb.append('[');
                for(int i = 0;i < regex.size();i++){
                    sb.append(regex.get(i).toString());
                    if(i < regex.size() - 1) sb.append(", ");
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
            sb.append("'").append(HASH.value).append("'");
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
                sb.append("'").append(LEFT.value).append("'");
                return sb.toString();
            }
        }
        public static class Sequenceg12{
            sequenceg1 holder;
            public Token RIGHT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(RIGHT.value).append("'");
                return sb.toString();
            }
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
            public Token SEPARATOR;
            public simple simple;
            public regexg1 type;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(name.toString());
                first = false;
                if(!first){
                    sb.append(", ");
                }
                sb.append("'").append(SEPARATOR.value).append("'");
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
                return sb.toString();
            }
        }
        public static class Regex2{
            regex holder;
            public simple simple;
            public regexg2 type;
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
                return sb.toString();
            }
        }
    }
    public static class regexg2{
        public int which;
        Regexg21 STAR;
        Regexg22 PLUS;
        Regexg23 QUES;
        public String toString(){
            StringBuilder sb = new StringBuilder("regexg2#" + which + "{");
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
        public static class Regexg21{
            regexg2 holder;
            public Token STAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(STAR.value).append("'");
                return sb.toString();
            }
        }
        public static class Regexg22{
            regexg2 holder;
            public Token PLUS;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(PLUS.value).append("'");
                return sb.toString();
            }
        }
        public static class Regexg23{
            regexg2 holder;
            public Token QUES;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(QUES.value).append("'");
                return sb.toString();
            }
        }
    }
    public static class regexg1{
        public int which;
        Regexg11 STAR;
        Regexg12 PLUS;
        Regexg13 QUES;
        public String toString(){
            StringBuilder sb = new StringBuilder("regexg1#" + which + "{");
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
        public static class Regexg11{
            regexg1 holder;
            public Token STAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(STAR.value).append("'");
                return sb.toString();
            }
        }
        public static class Regexg12{
            regexg1 holder;
            public Token PLUS;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(PLUS.value).append("'");
                return sb.toString();
            }
        }
        public static class Regexg13{
            regexg1 holder;
            public Token QUES;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(QUES.value).append("'");
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
                sb.append("'").append(EPSILON.value).append("'");
                return sb.toString();
            }
        }
        public static class Simple8{
            simple holder;
            public Token SHORTCUT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(SHORTCUT.value).append("'");
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
            sb.append("'").append(LP.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append(rhs.toString());
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(RP.value).append("'");
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
                sb.append("'").append(STRING.value).append("'");
                return sb.toString();
            }
        }
        public static class Stringnode2{
            stringNode holder;
            public Token CHAR;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(CHAR.value).append("'");
                return sb.toString();
            }
        }
    }
    public static class bracketNode{
        public Token BRACKET;
        public String toString(){
            StringBuilder sb = new StringBuilder("bracketNode{");
            boolean first = true;
            sb.append("'").append(BRACKET.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class untilNode{
        public Token TILDE;
        public regex regex;
        public String toString(){
            StringBuilder sb = new StringBuilder("untilNode{");
            boolean first = true;
            sb.append("'").append(TILDE.value).append("'");
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
            sb.append("'").append(DOT.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class name{
        public int which;
        Name1 IDENT;
        Name2 TOKEN;
        Name3 SKIP;
        Name4 OPTIONS;
        public String toString(){
            StringBuilder sb = new StringBuilder("name#" + which + "{");
            if(which == 1){
                sb.append(IDENT);
            }
            else if(which == 2){
                sb.append(TOKEN);
            }
            else if(which == 3){
                sb.append(SKIP);
            }
            else if(which == 4){
                sb.append(OPTIONS);
            }
            return sb.append("}").toString();
        }
        public static class Name1{
            name holder;
            public Token IDENT;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(IDENT.value).append("'");
                return sb.toString();
            }
        }
        public static class Name2{
            name holder;
            public Token TOKEN;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(TOKEN.value).append("'");
                return sb.toString();
            }
        }
        public static class Name3{
            name holder;
            public Token SKIP;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(SKIP.value).append("'");
                return sb.toString();
            }
        }
        public static class Name4{
            name holder;
            public Token OPTIONS;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append("'").append(OPTIONS.value).append("'");
                return sb.toString();
            }
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
            sb.append("'").append(CALL_BEGIN.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(IDENT.value).append("'");
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
            sb.append("'").append(RP.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class callg1{
        public Token COMMA;
        public Token IDENT;
        public String toString(){
            StringBuilder sb = new StringBuilder("callg1{");
            boolean first = true;
            sb.append("'").append(COMMA.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(IDENT.value).append("'");
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
            boolean first = true;
            sb.append("'").append(JOIN.value).append("'");
            first = false;
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(LP.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(COMMA.value).append("'");
            if(!first){
                sb.append(", ");
            }
            sb.append("'").append(RP.value).append("'");
            return sb.append("}").toString();
        }
    }
    public static class nameOrString{
        public int which;
        Nameorstring1 name;
        Nameorstring2 stringNode;
        public String toString(){
            StringBuilder sb = new StringBuilder("nameOrString#" + which + "{");
            if(which == 1){
                sb.append(name);
            }
            else if(which == 2){
                sb.append(stringNode);
            }
            return sb.append("}").toString();
        }
        public static class Nameorstring1{
            nameOrString holder;
            public name name;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(name.toString());
                return sb.toString();
            }
        }
        public static class Nameorstring2{
            nameOrString holder;
            public stringNode stringNode;
            public String toString(){
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(stringNode.toString());
                return sb.toString();
            }
        }
    }
}
